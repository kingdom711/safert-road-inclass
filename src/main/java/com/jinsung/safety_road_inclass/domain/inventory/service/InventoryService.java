package com.jinsung.safety_road_inclass.domain.inventory.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.inventory.dto.BoxOpenedResponse;
import com.jinsung.safety_road_inclass.domain.inventory.dto.InventoryItemResponse;
import com.jinsung.safety_road_inclass.domain.inventory.dto.InventoryResponse;
import com.jinsung.safety_road_inclass.domain.inventory.entity.BoxReward;
import com.jinsung.safety_road_inclass.domain.inventory.entity.BoxType;
import com.jinsung.safety_road_inclass.domain.inventory.entity.InventoryItemType;
import com.jinsung.safety_road_inclass.domain.inventory.entity.ItemBox;
import com.jinsung.safety_road_inclass.domain.inventory.entity.UserInventoryItem;
import com.jinsung.safety_road_inclass.domain.inventory.repository.BoxRewardRepository;
import com.jinsung.safety_road_inclass.domain.inventory.repository.ItemBoxRepository;
import com.jinsung.safety_road_inclass.domain.inventory.repository.UserInventoryItemRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * InventoryService - 인벤토리 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final UserInventoryItemRepository userInventoryItemRepository;
    private final ItemBoxRepository itemBoxRepository;
    private final BoxRewardRepository boxRewardRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    /**
     * 상자를 인벤토리에 추가
     */
    @Transactional
    public Long addBoxToInventory(Long userId, Long boxId, String source, Long sourceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ItemBox box = itemBoxRepository.findById(boxId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_NOT_FOUND));

        // 기존에 같은 상자가 있는지 확인
        UserInventoryItem existingItem = userInventoryItemRepository
                .findByUserIdAndItemIdAndItemType(userId, boxId.toString(), InventoryItemType.BOX)
                .orElse(null);

        if (existingItem != null) {
            // 수량 증가
            existingItem.addQuantity(1);
            userInventoryItemRepository.save(existingItem);
            log.info("상자 수량 증가: userId={}, boxId={}, quantity={}", userId, boxId, existingItem.getQuantity());
            return existingItem.getId();
        } else {
            // 새로 추가
            UserInventoryItem inventoryItem = UserInventoryItem.builder()
                    .user(user)
                    .itemId(boxId.toString())
                    .itemType(InventoryItemType.BOX)
                    .quantity(1)
                    .source(source)
                    .sourceId(sourceId)
                    .build();

            UserInventoryItem saved = userInventoryItemRepository.save(inventoryItem);
            log.info("상자 인벤토리 추가: userId={}, boxId={}", userId, boxId);
            return saved.getId();
        }
    }

    /**
     * 아이템을 인벤토리에 추가
     */
    @Transactional
    public Long addItemToInventory(Long userId, String itemId, int quantity, String source) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 기존에 같은 아이템이 있는지 확인
        UserInventoryItem existingItem = userInventoryItemRepository
                .findByUserIdAndItemIdAndItemType(userId, itemId, InventoryItemType.ITEM)
                .orElse(null);

        if (existingItem != null) {
            // 수량 증가
            existingItem.addQuantity(quantity);
            userInventoryItemRepository.save(existingItem);
            log.info("아이템 수량 증가: userId={}, itemId={}, quantity={}", userId, itemId, existingItem.getQuantity());
            return existingItem.getId();
        } else {
            // 새로 추가
            UserInventoryItem inventoryItem = UserInventoryItem.builder()
                    .user(user)
                    .itemId(itemId)
                    .itemType(InventoryItemType.ITEM)
                    .quantity(quantity)
                    .source(source)
                    .build();

            UserInventoryItem saved = userInventoryItemRepository.save(inventoryItem);
            log.info("아이템 인벤토리 추가: userId={}, itemId={}, quantity={}", userId, itemId, quantity);
            return saved.getId();
        }
    }

    /**
     * 상자 열기
     */
    @Transactional
    public BoxOpenedResponse openBox(Long userId, Long inventoryItemId) {
        // 1. 인벤토리 아이템 조회 및 검증
        UserInventoryItem inventoryItem = userInventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

        if (!inventoryItem.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        if (inventoryItem.getItemType() != InventoryItemType.BOX) {
            throw new CustomException(ErrorCode.BOX_NOT_FOUND);
        }

        if (inventoryItem.getQuantity() <= 0) {
            throw new CustomException(ErrorCode.BOX_NOT_FOUND);
        }

        // 2. 상자 정보 조회
        Long boxId = Long.parseLong(inventoryItem.getItemId());
        ItemBox box = itemBoxRepository.findByIdWithRewards(boxId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOX_NOT_FOUND));

        if (box.getRewards().isEmpty()) {
            throw new CustomException(ErrorCode.BOX_NO_REWARDS);
        }

        // 3. 가중치 기반 랜덤 보상 선택
        List<BoxReward> rewards = boxRewardRepository.findByBoxIdOrderByWeightDesc(boxId);
        BoxReward selectedReward = selectRandomReward(rewards);

        // 4. 수량 결정
        int quantity = selectedReward.getMinQuantity();
        if (selectedReward.getMaxQuantity() > selectedReward.getMinQuantity()) {
            quantity = random.nextInt(
                    selectedReward.getMaxQuantity() - selectedReward.getMinQuantity() + 1
            ) + selectedReward.getMinQuantity();
        }

        // 5. 아이템을 인벤토리에 추가
        addItemToInventory(userId, selectedReward.getItemId(), quantity, "상자 열기");

        // 6. 상자 수량 감소
        inventoryItem.subtractQuantity(1);
        if (inventoryItem.getQuantity() <= 0) {
            userInventoryItemRepository.delete(inventoryItem);
        } else {
            userInventoryItemRepository.save(inventoryItem);
        }

        // 7. 응답 생성
        List<BoxOpenedResponse.RewardedItem> rewardedItems = List.of(
                BoxOpenedResponse.RewardedItem.builder()
                        .itemId(selectedReward.getItemId())
                        .quantity(quantity)
                        .build()
        );

        log.info("상자 열기 완료: userId={}, boxId={}, itemId={}, quantity={}", userId, boxId, selectedReward.getItemId(), quantity);

        return BoxOpenedResponse.of(boxId, box.getName(), rewardedItems);
    }

    /**
     * 가중치 기반 랜덤 보상 선택
     */
    private BoxReward selectRandomReward(List<BoxReward> rewards) {
        if (rewards.isEmpty()) {
            throw new CustomException(ErrorCode.BOX_NO_REWARDS);
        }

        int totalWeight = rewards.stream()
                .mapToInt(BoxReward::getWeight)
                .sum();

        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (BoxReward reward : rewards) {
            currentWeight += reward.getWeight();
            if (randomValue < currentWeight) {
                return reward;
            }
        }

        // fallback (이론적으로 도달하지 않음)
        return rewards.get(0);
    }

    /**
     * 인벤토리 조회
     */
    public InventoryResponse getInventory(Long userId, InventoryItemType type) {
        List<UserInventoryItem> items;

        if (type != null) {
            items = userInventoryItemRepository.findByUserIdAndItemTypeOrderByCreatedAtDesc(userId, type);
        } else {
            items = userInventoryItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        List<InventoryItemResponse> itemResponses = items.stream()
                .map(InventoryItemResponse::from)
                .collect(Collectors.toList());

        if (type != null) {
            // 타입 필터링된 경우
            return InventoryResponse.of(
                    type == InventoryItemType.ITEM ? itemResponses : new ArrayList<>(),
                    type == InventoryItemType.BOX ? itemResponses : new ArrayList<>()
            );
        } else {
            // 전체 조회
            List<InventoryItemResponse> itemList = items.stream()
                    .filter(item -> item.getItemType() == InventoryItemType.ITEM)
                    .map(InventoryItemResponse::from)
                    .collect(Collectors.toList());

            List<InventoryItemResponse> boxList = items.stream()
                    .filter(item -> item.getItemType() == InventoryItemType.BOX)
                    .map(InventoryItemResponse::from)
                    .collect(Collectors.toList());

            return InventoryResponse.of(itemList, boxList);
        }
    }
}

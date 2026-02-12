-- =============================================
-- 출석 보상 시스템 초기 데이터 시딩
-- V4: Attendance Rewards Data Seeding
-- =============================================

-- =============================================
-- 1. 아이템 상자 마스터 데이터
-- =============================================
INSERT INTO item_boxes (name, box_type, description, image_url, active, created_at, updated_at)
VALUES
    ('일반 아이템 상자', 'NORMAL', '일반 등급의 아이템이 나올 수 있는 상자입니다.', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('고급 아이템 상자', 'RARE', '고급 등급의 아이템이 나올 수 있는 상자입니다.', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('희귀 아이템 상자', 'EPIC', '희귀 등급의 아이템이 나올 수 있는 상자입니다.', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('전설 아이템 상자', 'LEGENDARY', '전설 등급의 아이템이 나올 수 있는 상자입니다.', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('특별 아이템 상자', 'SPECIAL', '특별한 아이템이 나올 수 있는 상자입니다.', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- =============================================
-- 2. 상자 보상 풀 정의 (예시 - 실제 아이템 ID는 프론트엔드 itemsData와 매칭 필요)
-- =============================================
-- 일반 상자 보상 (가중치: 높을수록 확률 높음)
INSERT INTO box_rewards (box_id, item_id, weight, min_quantity, max_quantity, created_at)
SELECT id, 'item_001', 100, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'NORMAL'
UNION ALL
SELECT id, 'item_002', 80, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'NORMAL'
UNION ALL
SELECT id, 'item_003', 60, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'NORMAL'
ON CONFLICT DO NOTHING;

-- 고급 상자 보상
INSERT INTO box_rewards (box_id, item_id, weight, min_quantity, min_quantity, created_at)
SELECT id, 'item_010', 100, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'RARE'
UNION ALL
SELECT id, 'item_011', 80, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'RARE'
UNION ALL
SELECT id, 'item_012', 60, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'RARE'
ON CONFLICT DO NOTHING;

-- 희귀 상자 보상
INSERT INTO box_rewards (box_id, item_id, weight, min_quantity, max_quantity, created_at)
SELECT id, 'item_020', 100, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'EPIC'
UNION ALL
SELECT id, 'item_021', 80, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'EPIC'
UNION ALL
SELECT id, 'item_022', 60, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'EPIC'
ON CONFLICT DO NOTHING;

-- 전설 상자 보상
INSERT INTO box_rewards (box_id, item_id, weight, min_quantity, max_quantity, created_at)
SELECT id, 'item_030', 100, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'LEGENDARY'
UNION ALL
SELECT id, 'item_031', 80, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'LEGENDARY'
UNION ALL
SELECT id, 'item_032', 60, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'LEGENDARY'
ON CONFLICT DO NOTHING;

-- 특별 상자 보상
INSERT INTO box_rewards (box_id, item_id, weight, min_quantity, max_quantity, created_at)
SELECT id, 'item_040', 100, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'SPECIAL'
UNION ALL
SELECT id, 'item_041', 80, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'SPECIAL'
UNION ALL
SELECT id, 'item_042', 60, 1, 1, CURRENT_TIMESTAMP FROM item_boxes WHERE box_type = 'SPECIAL'
ON CONFLICT DO NOTHING;

-- =============================================
-- 3. 출석 보상 마스터 데이터 (Day 1~26)
-- =============================================
-- Day 1~8: 포인트 보상
INSERT INTO attendance_rewards (day_number, reward_type, points_amount, box_id, is_perfect_attendance, active, created_at, updated_at)
VALUES
    (1, 'POINTS', 30, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'POINTS', 40, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'POINTS', 50, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'POINTS', 60, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'POINTS', 70, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 'POINTS', 80, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 'POINTS', 90, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 'POINTS', 100, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (day_number, reward_type) DO NOTHING;

-- Day 9: 일반 아이템 상자
INSERT INTO attendance_rewards (day_number, reward_type, points_amount, box_id, is_perfect_attendance, active, created_at, updated_at)
SELECT 9, 'ITEM_BOX', 0, id, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM item_boxes WHERE box_type = 'NORMAL'
ON CONFLICT (day_number, reward_type) DO NOTHING;

-- Day 10~11: 포인트 보상
INSERT INTO attendance_rewards (day_number, reward_type, points_amount, box_id, is_perfect_attendance, active, created_at, updated_at)
VALUES
    (10, 'POINTS', 150, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 'POINTS', 200, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (day_number, reward_type) DO NOTHING;

-- Day 12: 고급 아이템 상자
INSERT INTO attendance_rewards (day_number, reward_type, points_amount, box_id, is_perfect_attendance, active, created_at, updated_at)
SELECT 12, 'ITEM_BOX', 0, id, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM item_boxes WHERE box_type = 'RARE'
ON CONFLICT (day_number, reward_type) DO NOTHING;

-- Day 13~14: 포인트 보상
INSERT INTO attendance_rewards (day_number, reward_type, points_amount, box_id, is_perfect_attendance, active, created_at, updated_at)
VALUES
    (13, 'POINTS', 250, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, 'POINTS', 300, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (day_number, reward_type) DO NOTHING;

-- Day 15: 희귀 아이템 상자
INSERT INTO attendance_rewards (day_number, reward_type, points_amount, box_id, is_perfect_attendance, active, created_at, updated_at)
SELECT 15, 'ITEM_BOX', 0, id, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM item_boxes WHERE box_type = 'EPIC'
ON CONFLICT (day_number, reward_type) DO NOTHING;

-- Day 16~18: 포인트 보상
INSERT INTO attendance_rewards (day_number, reward_type, points_amount, box_id, is_perfect_attendance, active, created_at, updated_at)
VALUES
    (16, 'POINTS', 350, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (17, 'POINTS', 400, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (18, 'POINTS', 450, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (day_number, reward_type) DO NOTHING;

-- Day 19: 전설 아이템 상자
INSERT INTO attendance_rewards (day_number, reward_type, points_amount, box_id, is_perfect_attendance, active, created_at, updated_at)
SELECT 19, 'ITEM_BOX', 0, id, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM item_boxes WHERE box_type = 'LEGENDARY'
ON CONFLICT (day_number, reward_type) DO NOTHING;

-- Day 20~24: 포인트 보상
INSERT INTO attendance_rewards (day_number, reward_type, points_amount, box_id, is_perfect_attendance, active, created_at, updated_at)
VALUES
    (20, 'POINTS', 500, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (21, 'POINTS', 600, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (22, 'POINTS', 700, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (23, 'POINTS', 800, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (24, 'POINTS', 1000, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (day_number, reward_type) DO NOTHING;

-- Day 25: 특별 아이템 상자
INSERT INTO attendance_rewards (day_number, reward_type, points_amount, box_id, is_perfect_attendance, active, created_at, updated_at)
SELECT 25, 'ITEM_BOX', 0, id, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM item_boxes WHERE box_type = 'SPECIAL'
ON CONFLICT (day_number, reward_type) DO NOTHING;

-- Day 26: 만근 대보상 (2000P + 전설 아이템 상자)
INSERT INTO attendance_rewards (day_number, reward_type, points_amount, box_id, is_perfect_attendance, active, created_at, updated_at)
SELECT 26, 'PERFECT_ATTENDANCE', 2000, id, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM item_boxes WHERE box_type = 'LEGENDARY'
ON CONFLICT (day_number, reward_type) DO NOTHING;

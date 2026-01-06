import os

build_gradle_path = r"d:\Workspace\repository\FULL\safert-road-inclass\build.gradle"

with open(build_gradle_path, 'r', encoding='utf-8') as f:
    content = f.read()

old_line = "implementation 'org.flywaydb:flyway-mysql'"
new_content = """implementation 'org.flywaydb:flyway-mysql'

\t// ULID Creator library (lexicographically sortable unique identifier)
\timplementation 'com.github.f4b6a3:ulid-creator:5.2.3'"""

updated_content = content.replace(old_line, new_content)

with open(build_gradle_path, 'w', encoding='utf-8') as f:
    f.write(updated_content)

print("build.gradle updated successfully!")

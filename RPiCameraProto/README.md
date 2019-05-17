RPiCameraProto
--------------

- Application for automatically capturing photos when approaching designated targets using the Raspberry Pi platform
- Can be utilized individually or in combination with backend_cmd (https://github.com/otula/apilta/tree/master/backend_cmd)
- Example target and configuration files can be found in PeopleCounter/templates

### Instructions

Modify camera.py to reflect the correct DATABASE_FILE path (for sqlite results), PHOTO_DIRECTORY (location for caputred photos), TARGETS_FILE (list of targets if database is not used)  and TASK_ID (if hardcoded task identifiers are used).

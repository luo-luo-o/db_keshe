/*
  Oracle 21c database timezone bootstrap.

  Run this script as SYSTEM or SYSDBA before oracle21c-init.sql when creating
  or resetting the database.

  Notes:
  - DBTIMEZONE changes are not reflected immediately in the current instance.
  - Restart the Oracle instance/container after running this script, then run
    oracle21c-init.sql as the application user.
*/

WHENEVER SQLERROR EXIT SQL.SQLCODE

ALTER SESSION SET TIME_ZONE = 'Asia/Shanghai';
ALTER DATABASE SET TIME_ZONE = '+08:00';

PROMPT Database timezone change submitted. Restart Oracle before running oracle21c-init.sql.

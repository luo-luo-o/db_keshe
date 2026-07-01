/*
  Oracle 21c Data Pump directory bootstrap.

  Run this script as SYSTEM or SYSDBA in the XEPDB1 pluggable database before
  using application backups. Enter the value of DB_USERNAME from .env when
  prompted. Press Enter to use the default psm_app user.
*/

WHENEVER SQLERROR EXIT SQL.SQLCODE

ACCEPT APP_USER CHAR DEFAULT 'PSM_APP' PROMPT 'Application database user [PSM_APP]: '

CREATE OR REPLACE DIRECTORY PSM_BACKUP_DIR AS '/opt/oracle/psm-backups';
GRANT READ, WRITE ON DIRECTORY PSM_BACKUP_DIR TO &&APP_USER;

PROMPT Data Pump directory PSM_BACKUP_DIR is ready for &&APP_USER.

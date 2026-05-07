/*
  PSM-Smart Oracle 21c initialization entrypoint.

  Run this script from SQL*Plus / SQLcl / compatible database tools in the
  Core/src/main/resources/db directory.

  Script layout:
  - oracle21c-schema.sql: database framework, including tables, constraints,
    comments, sequences, and indexes.
  - oracle21c-base-data.sql: deployment baseline data, including users,
    maintenance people, station metadata, devices, tags, and thresholds.
  - oracle21c-mock-data.sql: demo/mock runtime data. TODO: replace current
    placeholder rows with realistic simulation output after acquisition logic
    is implemented.
*/

@@oracle21c-schema.sql
@@oracle21c-base-data.sql
@@oracle21c-mock-data.sql


-- Drop unused PostgreSQL enum types (columns use VARCHAR instead)
DROP TYPE IF EXISTS status_os;
DROP TYPE IF EXISTS status_service;

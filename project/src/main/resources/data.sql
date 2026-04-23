-- ==================== PETS ====================
INSERT INTO pets (name, species, breed, birth_date, weight, tutor_name, tutor_phone, active)
VALUES ('Thor', 'DOG', 'Golden Retriever', '2020-03-15', 28.5, 'Carlos Oliveira', '(11) 99999-1111', true);

INSERT INTO pets (name, species, breed, birth_date, weight, tutor_name, tutor_phone, active)
VALUES ('Luna', 'CAT', 'Siamês', '2021-07-20', 4.2, 'Ana Lima', '(11) 98888-2222', true);

INSERT INTO pets (name, species, breed, birth_date, weight, tutor_name, tutor_phone, active)
VALUES ('Bob', 'DOG', 'Labrador', '2019-11-05', 32.0, 'Pedro Santos', '(11) 97777-3333', true);

-- ==================== VACCINES ====================
INSERT INTO vaccines (pet_id, name, application_date, due_date, status)
VALUES (1, 'V10 - Polivalente', '2024-03-10', '2025-03-10', 'OVERDUE');

INSERT INTO vaccines (pet_id, name, application_date, due_date, status)
VALUES (1, 'Antirrábica', '2024-06-01', '2025-06-01', 'EXPIRING_SOON');

INSERT INTO vaccines (pet_id, name, application_date, due_date, status)
VALUES (2, 'Quádrupla Felina', '2024-07-15', '2025-07-15', 'PENDING');

INSERT INTO vaccines (pet_id, name, application_date, due_date, status)
VALUES (2, 'Antirrábica Felina', '2024-09-01', '2025-09-01', 'PENDING');

INSERT INTO vaccines (pet_id, name, application_date, due_date, status)
VALUES (3, 'V10 - Polivalente', '2024-11-01', '2025-11-01', 'APPLIED');

-- ==================== ROUTINE RECORDS ====================
INSERT INTO routine_records (pet_id, type, description, record_date)
VALUES (1, 'WALK', 'Caminhada de 30 minutos no parque', '2025-04-20');

INSERT INTO routine_records (pet_id, type, description, record_date)
VALUES (1, 'BATHING', 'Banho e tosa completo', '2025-04-15');

INSERT INTO routine_records (pet_id, type, description, record_date)
VALUES (1, 'VET_VISIT', 'Check-up anual. Tudo ok, peso ideal.', '2025-04-10');

INSERT INTO routine_records (pet_id, type, description, record_date)
VALUES (2, 'MEDICATION', 'Vermifugação preventiva', '2025-04-18');

INSERT INTO routine_records (pet_id, type, description, record_date)
VALUES (2, 'GROOMING', 'Escovação e limpeza de orelhas', '2025-04-22');

INSERT INTO routine_records (pet_id, type, description, record_date)
VALUES (3, 'WALK', 'Corrida de 45 minutos na praia', '2025-04-21');

-- ==================== ALERTS ====================
INSERT INTO alerts (pet_id, type, message, due_date, sent, created_at)
VALUES (1, 'VACCINE_OVERDUE', 'Vacina V10 do pet Thor está VENCIDA desde 10/03/2025. Atualize urgente!', '2025-03-10', false, CURRENT_TIMESTAMP);

INSERT INTO alerts (pet_id, type, message, due_date, sent, created_at)
VALUES (1, 'VACCINE_DUE', 'Vacina Antirrábica do pet Thor vence em 01/06/2025. Agende a dose de reforço!', '2025-06-01', false, CURRENT_TIMESTAMP);

INSERT INTO alerts (pet_id, type, message, due_date, sent, created_at)
VALUES (2, 'VACCINE_DUE', 'Vacina Quádrupla Felina da pet Luna vence em 15/07/2025.', '2025-07-15', false, CURRENT_TIMESTAMP);

INSERT INTO alerts (pet_id, type, message, due_date, sent, created_at)
VALUES (3, 'HEALTH_CHECK', 'Bob está há mais de 6 meses sem visita ao veterinário.', '2025-05-05', false, CURRENT_TIMESTAMP);


-- =====================================================================
-- PetOS - massa de dados de DESENVOLVIMENTO
--
-- Executada apenas quando o perfil "dev" esta ativo
-- (spring.flyway.locations inclui classpath:db/dev).
-- As datas sao relativas a data de execucao para que os estados
-- (vencida / a vencer / em dia) continuem coerentes com a politica
-- de vacinacao ao longo do tempo.
-- Os usuarios de desenvolvimento sao criados pelo DevelopmentDataSeeder,
-- que aplica o hash BCrypt das senhas (nenhum hash e versionado aqui).
-- =====================================================================

INSERT INTO pets (name, species, breed, birth_date, weight, tutor_name, tutor_phone, active)
VALUES ('Thor', 'DOG', 'Golden Retriever', DATEADD('YEAR', -6, CURRENT_DATE), 28.5, 'Carlos Oliveira', '(11) 99999-1111', TRUE),
       ('Luna', 'CAT', 'Siamês', DATEADD('YEAR', -5, CURRENT_DATE), 4.2, 'Carlos Oliveira', '(11) 99999-1111', TRUE),
       ('Bob', 'DOG', 'Labrador', DATEADD('YEAR', -7, CURRENT_DATE), 32.0, 'Pedro Santos', '(11) 97777-3333', TRUE);

-- Vacina vencida: dispara alerta VACCINE_OVERDUE
INSERT INTO vaccines (pet_id, name, application_date, due_date, status)
SELECT p.id, 'V10 - Polivalente', DATEADD('MONTH', -13, CURRENT_DATE), DATEADD('DAY', -20, CURRENT_DATE), 'OVERDUE'
FROM pets p WHERE p.name = 'Thor';

-- Vacina dentro da janela preventiva de 30 dias: dispara alerta VACCINE_DUE
INSERT INTO vaccines (pet_id, name, application_date, due_date, status)
SELECT p.id, 'Antirrábica', DATEADD('MONTH', -11, CURRENT_DATE), DATEADD('DAY', 15, CURRENT_DATE), 'EXPIRING_SOON'
FROM pets p WHERE p.name = 'Thor';

-- Vacinas em dia: nenhum alerta preventivo
INSERT INTO vaccines (pet_id, name, application_date, due_date, status)
SELECT p.id, 'Quádrupla Felina', DATEADD('MONTH', -2, CURRENT_DATE), DATEADD('MONTH', 10, CURRENT_DATE), 'APPLIED'
FROM pets p WHERE p.name = 'Luna';

INSERT INTO vaccines (pet_id, name, application_date, due_date, status)
SELECT p.id, 'V10 - Polivalente', DATEADD('MONTH', -1, CURRENT_DATE), DATEADD('MONTH', 11, CURRENT_DATE), 'APPLIED'
FROM pets p WHERE p.name = 'Bob';

INSERT INTO routine_records (pet_id, type, description, record_date)
SELECT p.id, 'WALK', 'Caminhada de 30 minutos no parque', DATEADD('DAY', -2, CURRENT_DATE)
FROM pets p WHERE p.name = 'Thor';

INSERT INTO routine_records (pet_id, type, description, record_date)
SELECT p.id, 'VET_VISIT', 'Check-up anual. Peso ideal.', DATEADD('DAY', -30, CURRENT_DATE)
FROM pets p WHERE p.name = 'Thor';

INSERT INTO routine_records (pet_id, type, description, record_date)
SELECT p.id, 'MEDICATION', 'Vermifugação preventiva', DATEADD('DAY', -5, CURRENT_DATE)
FROM pets p WHERE p.name = 'Luna';

INSERT INTO routine_records (pet_id, type, description, record_date)
SELECT p.id, 'GROOMING', 'Escovação e limpeza de orelhas', DATEADD('DAY', -1, CURRENT_DATE)
FROM pets p WHERE p.name = 'Bob';

-- Alertas preventivos derivados das vacinas, ja vinculados a vacina de origem,
-- exatamente como o fluxo de vacinacao os geraria em tempo de execucao.
INSERT INTO alerts (pet_id, vaccine_id, type, message, due_date, sent, created_at)
SELECT v.pet_id, v.id, 'VACCINE_OVERDUE',
       'Vacina ''' || v.name || ''' do pet ' || p.name || ' está vencida. Regularize o quanto antes.',
       v.due_date, FALSE, CURRENT_TIMESTAMP
FROM vaccines v JOIN pets p ON p.id = v.pet_id
WHERE v.status = 'OVERDUE';

INSERT INTO alerts (pet_id, vaccine_id, type, message, due_date, sent, created_at)
SELECT v.pet_id, v.id, 'VACCINE_DUE',
       'Vacina ''' || v.name || ''' do pet ' || p.name || ' está próxima do vencimento. Agende a dose de reforço.',
       v.due_date, FALSE, CURRENT_TIMESTAMP
FROM vaccines v JOIN pets p ON p.id = v.pet_id
WHERE v.status = 'EXPIRING_SOON';


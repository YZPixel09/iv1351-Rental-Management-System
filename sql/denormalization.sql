--Copy Individual Lessons--
INSERT INTO history_lesson (lesson_id, student_id, lesson_type, genre, instrument, lesson_price, student_name, student_email)
SELECT 
    l.lesson_id,
    sl.student_id,
    'Individual' AS lesson_type,
    NULL AS genre,
    l.instrument_type AS instrument,
    lp.skill_level_price + lp.lesson_type_price AS lesson_price,
    CONCAT(p.first_name, ' ', p.last_name) AS student_name,
    e.email AS student_email
FROM lesson l
JOIN individual_lesson il ON l.lesson_id = il.lesson_id
JOIN student_lessons sl ON l.lesson_id = sl.lesson_id
JOIN student s ON sl.student_id = s.student_id
JOIN person p ON s.person_id = p.person_id
JOIN person_email pe ON p.person_id = pe.person_id
JOIN email e ON pe.email_id = e.email_id
JOIN lesson_price_history lp ON l.lesson_price_id = lp.lesson_price_id;

--Copy Group Lessons--
INSERT INTO history_lesson (lesson_id, student_id, lesson_type, genre, instrument, lesson_price, student_name, student_email)
SELECT 
    l.lesson_id,
    sl.student_id,
    'Group' AS lesson_type,
    NULL AS genre,
    l.instrument_type AS instrument,
    lp.skill_level_price + lp.lesson_type_price AS lesson_price,
    CONCAT(p.first_name, ' ', p.last_name) AS student_name,
    e.email AS student_email
FROM lesson l
JOIN group_lesson gl ON l.lesson_id = gl.lesson_id
JOIN student_lessons sl ON l.lesson_id = sl.lesson_id
JOIN student s ON sl.student_id = s.student_id
JOIN person p ON s.person_id = p.person_id
JOIN person_email pe ON p.person_id = pe.person_id
JOIN email e ON pe.email_id = e.email_id
JOIN lesson_price_history lp ON l.lesson_price_id = lp.lesson_price_id;


--Copy Ensemble Lessons--
INSERT INTO history_lesson (lesson_id, student_id, lesson_type, genre, instrument, lesson_price, student_name, student_email)
SELECT 
    l.lesson_id,
    sl.student_id,
    'Ensemble' AS lesson_type,
    el.genre AS genre,
    NULL AS instrument,
    lp.skill_level_price + lp.lesson_type_price AS lesson_price,
    CONCAT(p.first_name, ' ', p.last_name) AS student_name,
    e.email AS student_email
FROM lesson l
JOIN ensembles_lesson el ON l.lesson_id = el.lesson_id
JOIN student_lessons sl ON l.lesson_id = sl.lesson_id
JOIN student s ON sl.student_id = s.student_id
JOIN person p ON s.person_id = p.person_id
JOIN person_email pe ON p.person_id = pe.person_id
JOIN email e ON pe.email_id = e.email_id
JOIN lesson_price_history lp ON l.lesson_price_id = lp.lesson_price_id;

SELECT * FROM history_lesson ORDER BY student_id;

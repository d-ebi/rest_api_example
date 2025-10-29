-- Sample data for users and career histories
INSERT OR IGNORE INTO users (id, name, age, birthday, height, zip_code, created_at, updated_at) VALUES
 (1, 'Taro Yamada', 30, '1994/04/01', 170.5, '123-4567', datetime('now'), datetime('now')),
 (2, 'Hanako Suzuki', 25, '1999/05/12', 160.0, '234-5678', datetime('now'), datetime('now')),
 (3, 'Ichiro Tanaka', 41, '1983/09/30', NULL, '345-6789', datetime('now'), datetime('now'));

INSERT OR IGNORE INTO career_histories (user_id, title, period_from, period_to) VALUES
 (1, 'Software Engineer', '2018/04/01', '2021/03/31'),
 (1, 'Senior Engineer',   '2021/04/01', '2024/03/31'),
 (2, 'QA Engineer',       '2020/06/01', '2022/03/31'),
 (3, 'Support',           '2008/04/01', '2012/03/31');

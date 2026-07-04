-- 数据字典表
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id SERIAL PRIMARY KEY,
    dict_code VARCHAR(100) NOT NULL UNIQUE,
    dict_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_dict_data (
    id SERIAL PRIMARY KEY,
    dict_code VARCHAR(100) NOT NULL,
    item_value VARCHAR(100) NOT NULL,
    item_label VARCHAR(100) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 待办事项表新增字段
ALTER TABLE todos ADD COLUMN IF NOT EXISTS creator VARCHAR(50);
ALTER TABLE todos ADD COLUMN IF NOT EXISTS assignee VARCHAR(50);
ALTER TABLE todos ADD COLUMN IF NOT EXISTS completed_at VARCHAR(30);

-- 备忘录表新增字段
ALTER TABLE notes ADD COLUMN IF NOT EXISTS note_type VARCHAR(50);

-- 初始化数据字典数据
INSERT INTO sys_dict_type (dict_code, dict_name, description, status) VALUES
('account_category', '记账分类', '记账管理的消费分类', 1),
('todo_priority', '待办优先级', '待办事项的优先级', 1),
('note_type', '备忘录类型', '备忘录的类型分类', 1)
ON CONFLICT (dict_code) DO NOTHING;

INSERT INTO sys_dict_data (dict_code, item_value, item_label, sort_order, status) VALUES
('account_category', 'food', '餐饮', 1, 1),
('account_category', 'transport', '交通', 2, 1),
('account_category', 'shopping', '购物', 3, 1),
('account_category', 'entertainment', '娱乐', 4, 1),
('account_category', 'housing', '住房', 5, 1),
('account_category', 'medical', '医疗', 6, 1),
('account_category', 'education', '教育', 7, 1),
('account_category', 'other', '其他', 99, 1),
('todo_priority', 'high', '高', 1, 1),
('todo_priority', 'normal', '中', 2, 1),
('todo_priority', 'low', '低', 3, 1),
('note_type', 'work', '工作', 1, 1),
('note_type', 'life', '生活', 2, 1),
('note_type', 'study', '学习', 3, 1),
('note_type', 'other', '其他', 99, 1)
ON CONFLICT DO NOTHING;
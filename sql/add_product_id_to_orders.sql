-- 1. 添加 product_id 字段
ALTER TABLE orders ADD COLUMN product_id INT AFTER product_name;

-- 2. 回填已有数据：通过 product_name 匹配 product 表，补齐 product_id
UPDATE orders o
    JOIN product p ON o.product_name = p.product_name
SET o.product_id = p.id
WHERE o.product_id IS NULL;

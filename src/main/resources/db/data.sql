-- ============================================================
-- Default Admin User
-- Username: admin
-- Password: admin123
-- Hash: BCrypt cost 10, verified with Spring Security BCryptPasswordEncoder
-- ============================================================


-- ============================================================
-- Sample Products
-- ============================================================
-- ============================================================
-- Sample Products
-- ============================================================
INSERT INTO product (name, current_price, stock_quantity, hsn_code, description, active, created_at)
SELECT 'Wooden Sofa Set (3+1+1)', 45000.00, 10, '9401', 'Premium teak wood sofa set with cushions, 5-seater', 1, CURRENT_TIMESTAMP
FROM dual WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Wooden Sofa Set (3+1+1)');

INSERT INTO product (name, current_price, stock_quantity, hsn_code, description, active, created_at)
SELECT 'King Size Bed Frame', 28000.00, 15, '9403', 'Solid sheesham wood king-size bed with storage', 1, CURRENT_TIMESTAMP
FROM dual WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'King Size Bed Frame');

INSERT INTO product (name, current_price, stock_quantity, hsn_code, description, active, created_at)
SELECT 'Dining Table Set (6 Chairs)', 32000.00, 8, '9403', 'Mango wood dining table with 6 cushioned chairs', 1, CURRENT_TIMESTAMP
FROM dual WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Dining Table Set (6 Chairs)');

INSERT INTO product (name, current_price, stock_quantity, hsn_code, description, active, created_at)
SELECT 'Wardrobe (3-Door)', 22000.00, 12, '9403', '3-door sliding wardrobe with mirror and internal shelves', 1, CURRENT_TIMESTAMP
FROM dual WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Wardrobe (3-Door)');

INSERT INTO product (name, current_price, stock_quantity, hsn_code, description, active, created_at)
SELECT 'Office Chair (Ergonomic)', 8500.00, 25, '9401', 'High-back ergonomic office chair with lumbar support', 1, CURRENT_TIMESTAMP
FROM dual WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Office Chair (Ergonomic)');

INSERT INTO product (name, current_price, stock_quantity, hsn_code, description, active, created_at)
SELECT 'Coffee Table (Glass Top)', 6500.00, 20, '9403', 'Wooden frame coffee table with tempered glass top', 1, CURRENT_TIMESTAMP
FROM dual WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Coffee Table (Glass Top)');

INSERT INTO product (name, current_price, stock_quantity, hsn_code, description, active, created_at)
SELECT 'Bookshelf (5-Tier)', 9000.00, 18, '9403', '5-tier open bookshelf in natural wood finish', 1, CURRENT_TIMESTAMP
FROM dual WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Bookshelf (5-Tier)');

INSERT INTO product (name, current_price, stock_quantity, hsn_code, description, active, created_at)
SELECT 'Recliner Chair (Single)', 18000.00, 6, '9401', 'Leather recliner with footrest and armrests', 1, CURRENT_TIMESTAMP
FROM dual WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Recliner Chair (Single)');

-- ============================================================
-- Sample Customers
-- ============================================================
INSERT INTO customer (name, phone, address, gst_number, active, created_at)
SELECT 'Rahul Sharma', '9876543210', '45, Indiranagar, Bengaluru - 560038', NULL, 1, CURRENT_TIMESTAMP
FROM dual WHERE NOT EXISTS (SELECT 1 FROM customer WHERE phone = '9876543210');

INSERT INTO customer (name, phone, address, gst_number, active, created_at)
SELECT 'ABC Interiors Pvt Ltd', '9123456789', '12, Commercial Street, Bengaluru - 560001', '29AABCA1234B1ZP', 1, CURRENT_TIMESTAMP
FROM dual WHERE NOT EXISTS (SELECT 1 FROM customer WHERE phone = '9123456789');

INSERT INTO customer (name, phone, address, gst_number, active, created_at)
SELECT 'Priya Patel', '9988776655', '78, JP Nagar, Bengaluru - 560078', NULL, 1, CURRENT_TIMESTAMP
FROM dual WHERE NOT EXISTS (SELECT 1 FROM customer WHERE phone = '9988776655');
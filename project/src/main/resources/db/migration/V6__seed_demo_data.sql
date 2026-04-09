-- Demo seed data to make the end-to-end Kafka flow testable out-of-the-box.
-- Uses conditional INSERTs so it is safe to run on existing databases.

INSERT INTO venue (id, name, address, total_capacity)
SELECT 1, 'Main Venue', '123 Demo Street', 1000
WHERE NOT EXISTS (SELECT 1 FROM venue WHERE id = 1);

INSERT INTO event (id, name, venue_id, total_capacity, left_capacity, ticket_price)
SELECT 1, 'Demo Event', 1, 100, 100, 20.00
WHERE NOT EXISTS (SELECT 1 FROM event WHERE id = 1);

INSERT INTO customer (id, name, email, address)
SELECT 1, 'Demo Customer', 'demo@example.com', '1 Demo Address'
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE id = 1);

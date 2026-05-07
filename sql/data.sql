-- Seed data for Product, Depot, and Stock

INSERT INTO Product (prodid, pname, price) VALUES
    ('p1', 'tape', 2.50),
    ('p2', 'tv',   250.00),
    ('p3', 'vcr',  80.00);

INSERT INTO Depot (depid, addr, volume) VALUES
    ('d1', 'New York',  9000),
    ('d2', 'Syracuse',  6000),
    ('d4', 'New York',  2000);

INSERT INTO Stock (prodid, depid, quantity) VALUES
    ('p1', 'd1',  1000),
    ('p1', 'd2', -1000),
    ('p1', 'd4',  1200),
    ('p3', 'd1',  3000),
    ('p3', 'd4',  2000),
    ('p2', 'd4',  1500),
    ('p2', 'd1',  -400),
    ('p2', 'd2',  2000);

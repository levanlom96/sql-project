
DROP TABLE IF EXISTS Stock;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS Depot;


CREATE TABLE Product (
    prodid  VARCHAR(50),
    pname   VARCHAR(100),
    price   NUMERIC(10, 2)
);

CREATE TABLE Depot (
    depid   VARCHAR(50),
    addr    VARCHAR(100),
    volume  INT
);

CREATE TABLE Stock (
    prodid   VARCHAR(50),
    depid    VARCHAR(50),
    quantity INT
);


ALTER TABLE Product
    ADD CONSTRAINT pk_product PRIMARY KEY (prodid);

ALTER TABLE Depot
    ADD CONSTRAINT pk_depot PRIMARY KEY (depid);

ALTER TABLE Stock
    ADD CONSTRAINT pk_stock PRIMARY KEY (prodid, depid);


ALTER TABLE Stock
    ADD CONSTRAINT fk_product FOREIGN KEY (prodid)
        REFERENCES Product(prodid)
        ON DELETE CASCADE
        ON UPDATE CASCADE;

ALTER TABLE Stock
    ADD CONSTRAINT fk_depot FOREIGN KEY (depid)
        REFERENCES Depot(depid)
        ON DELETE CASCADE
        ON UPDATE CASCADE;

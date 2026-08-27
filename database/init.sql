CREATE TABLE IF NOT EXISTS blueprints (
    author VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (author, name)
);

CREATE TABLE IF NOT EXISTS blueprint_points (
    id BIGSERIAL PRIMARY KEY,
    author VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,

    CONSTRAINT fk_blueprint
        FOREIGN KEY (author, name)
        REFERENCES blueprints(author, name)
        ON DELETE CASCADE
);

INSERT INTO blueprints (author, name)
VALUES
    ('john', 'house'),
    ('john', 'garage'),
    ('jane', 'garden')
ON CONFLICT (author, name) DO NOTHING;

INSERT INTO blueprint_points (author, name, x, y)
VALUES
    ('john', 'house', 0, 0),
    ('john', 'house', 10, 0),
    ('john', 'house', 10, 10),
    ('john', 'house', 0, 10),

    ('john', 'garage', 5, 5),
    ('john', 'garage', 15, 5),
    ('john', 'garage', 15, 15),

    ('jane', 'garden', 2, 2),
    ('jane', 'garden', 3, 4),
    ('jane', 'garden', 6, 7);
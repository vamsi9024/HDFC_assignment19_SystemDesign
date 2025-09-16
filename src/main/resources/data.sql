-- src/main/resources/data.sql
INSERT INTO movies(title, language, genre, duration) VALUES
('Inception','English','Sci-Fi',148),
('3 Idiots','Hindi','Comedy-Drama',170);

INSERT INTO theaters(name, city) VALUES
('PVR Phoenix','Mumbai'),
('INOX Malad','Mumbai');

-- Assume generated IDs or use explicit IDs in schema.sql
-- Create a couple of shows and seats for them
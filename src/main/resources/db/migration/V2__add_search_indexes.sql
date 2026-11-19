-- V2__add_search_indexes.sql
-- Thêm các chỉ mục (indexes) tối ưu tìm kiếm và truy vấn danh sách

CREATE INDEX idx_songs_title ON songs (title);
CREATE INDEX idx_songs_artist ON songs (artist);
CREATE INDEX idx_songs_genre ON songs (genre);
CREATE INDEX idx_songs_play_count ON songs (play_count);

CREATE INDEX idx_playlists_user_id ON playlists (user_id);

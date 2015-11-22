CREATE TABLE users (
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   firstname CHAR(100),
   lastname CHAR(100),
   login CHAR(100) NOT NULL,
   email CHAR(100),
   password CHAR(40) NOT NULL, -- sha1
   is_admin INTEGER NOT NULL CHECK (is_admin IN (0,1)) DEFAULT 0
   );

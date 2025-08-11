# SecureVault
A secure Java-based password manager that stores credentials in an SQLite database with AES encryption. Built for security and simplicity. Initially developed as a command-line tool, with plans to upgrade to a Swing-based GUI.


Author
Aamir A. Khan
📧 amir94_7@live.co.uk

Technologies Used
Core Java – Application logic and structure

Hashing – MessageDigest for master password hashing (with salt & iterations)

Database – SQLite for persistent storage

Encryption – AES for encrypting stored service passwords

Features (Planned)
User registration and login with hashed & salted master password

Secure AES encryption for stored credentials

Add, view, search, and delete password entries

Support for multiple users

Export/Import encrypted password backups

GUI version with Swing (future release)

Database Structure
Table: users

id (PK)

username

password_hash

salt

Table: passwords

id (PK)

user_id (FK → users.id)

service

username

password_encrypted

iv

Setup Instructions
Install Java (JDK 8 or higher).

Install VS Code with Java Extension Pack.

Download SQLite JDBC driver and place in lib/ folder.

Create the database using schema.sql.

Compile & run:

bash
Copy
Edit
javac -d bin -cp ".;lib/sqlite-jdbc.jar" src/*.java
java -cp ".;lib/sqlite-jdbc.jar;bin" Main


Security Considerations
Hashing: Currently uses MessageDigest (SHA-256) with unique per-user salts and multiple iterations for hashing the master password.

Encryption: Uses AES with a unique initialization vector (IV) for each password entry.

Limitations:

MessageDigest is not as secure as modern password hashing algorithms like PBKDF2, bcrypt, or scrypt because it’s computationally fast and more vulnerable to brute-force attacks.

In future versions, hashing should be upgraded to PBKDF2 or bcrypt for stronger password protection.

Best Practice Reminder: The database file alone should not be considered safe storage — it must be protected by filesystem permissions, backups should be encrypted, and the application should avoid exposing decrypted passwords unnecessarily.
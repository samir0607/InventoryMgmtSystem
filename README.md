# Inventory Management System

This repository contains a **Java-based Inventory Management System** with a server-client architecture. The system supports adding and viewing categories, suppliers, and products, offering a user-friendly GUI for client-side interaction and robust multithreading on the server side.

## Features

### Server
- **Add Categories, Suppliers, and Products**: The server handles requests to add these entities.
- **View Data**: Clients can view all categories, suppliers, or products stored on the server.
- **Thread-safe**: Concurrent access is managed using thread-safe collections.
- **Scalable**: The server can handle multiple client connections simultaneously.

### Client
- **Interactive GUI**: A graphical interface for managing and viewing inventory data.
- **Separate Panels**: Dedicated panels for viewing categories, suppliers, and products.
- **Refresh and Back**: Each view panel has refresh and back buttons for smooth navigation.

## Technologies Used
- **Java SE 8 or higher**
- **Swing**: For GUI development.
- **Multithreading**: For handling multiple clients.
- **Socket Programming**: For client-server communication.

## Setup and Usage

### Prerequisites
1. Java Development Kit (JDK) 8 or higher installed.
2. IDE or terminal for compiling and running Java programs.

### Steps

#### Server
1. Navigate to the `server` folder.
2. Compile and run the `Server.java` file.
   ```bash
   javac Server.java
   java Server
   ```
3. The server will start listening on port `12345`.

#### Client
1. Navigate to the `client` folder.
2. Compile and run the `ClientGUI.java` file.
   ```bash
   javac ClientGUI.java
   java ClientGUI
   ```
3. Use the GUI to interact with the server by adding or viewing data.

### Functionalities
- **Add Category**: Enter a category name and submit.
- **Add Supplier**: Provide a supplier name and contact details.
- **Add Product**: Enter product details including category ID, supplier ID, and price.
- **View Data**: Use dedicated view panels to view categories, suppliers, and products.


## Example Screenshots
<img width="907" alt="Screenshot 2024-12-14 at 4 54 36 PM" src="https://github.com/user-attachments/assets/27b5c948-665e-4d6e-a3e7-0ac2ae16112a" />

<img width="898" alt="Screenshot 2024-12-14 at 4 54 28 PM" src="https://github.com/user-attachments/assets/a46ab18f-6dcc-457f-a0a2-ae6b1fcb37a1" />

<img width="897" alt="Screenshot 2024-12-14 at 4 54 21 PM" src="https://github.com/user-attachments/assets/074161c8-6d55-495c-8ca2-5ed411a8b532" />

<img width="901" alt="Screenshot 2024-12-14 at 4 49 15 PM" src="https://github.com/user-attachments/assets/bc5689db-2019-4d40-8e6c-8b43dda48e0e" />

<img width="901" alt="Screenshot 2024-12-14 at 4 48 21 PM" src="https://github.com/user-attachments/assets/57d952f2-bf01-45c2-908d-f4f48c4364c0" />



### Server
```
Server is running on port 6090...
Client connected!
```



## Contributing
Feel free to fork this repository and contribute enhancements or bug fixes. Open a pull request with a detailed description of the changes.

## License
This project is licensed under the MIT License.

## Contact
For any issues or suggestions, please reach out via [GitHub Issues](https://github.com/your-repo-link/issues).


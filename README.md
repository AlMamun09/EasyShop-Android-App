EasyShop: Android POS & Inventory App
=====================================

EasyShop is a native Android application built with Java for managing a small shop's inventory right from your phone. It's a straightforward Point of Sale (POS) system that handles user accounts and lets you perform full CRUD (Create, Read, Update, Delete) operations on your product catalog. All data is stored locally on the device in a SQLite database.

Let's Break It Down
-------------------

Here's the thing: managing a small inventory can get messy. This app is designed to be a clean, simple solution. It's not bloated with features you don't need, focusing instead on the core tasks of managing what you sell.

The main user flow is simple:

1.  **Sign Up or Log In:** The app starts with a secure login and registration system. A "Remember Me" option is available to keep you logged in for 24 hours.

2.  **View Your Inventory:** After logging in, you land on the home page, which displays all your products in a clean grid layout.

3.  **Manage Products:** You can easily add new products, complete with a name, price, available units, and a photo from your gallery.

4.  **Update or Delete:** A simple long-press on any product card brings up a dialog to quickly update its details or remove it from your inventory.

Key Features
------------

-   **Full User Authentication:** Secure sign-up and login system to protect your shop's data.

-   **Persistent Login:** A "Remember Me" feature using `SharedPreferences` provides a convenient login experience.

-   **Complete Product Management (CRUD):**

    -   **Create:** Add new products with details and an image.

    -   **Read:** View all products in a `RecyclerView` grid on the main screen.

    -   **Update:** Modify existing product information, including the image.

    -   **Delete:** Remove products from your inventory with a confirmation step.

-   **Local SQLite Database:** All user and product data is stored securely and efficiently on the device. The `DBHelper` class uses a singleton pattern to ensure a single, stable database connection.

-   **Image Handling:** Pick product images from the device gallery. The app correctly handles modern storage permissions and uses the powerful **Glide** library for smooth, asynchronous image loading.

-   **Clean, Responsive UI:** The interface is built with standard Android XML layouts and components, including a `DrawerLayout` for navigation.

Tech Stack & Libraries
----------------------

This project is a classic native Android app built with a focus on fundamentals.

-   **Language:** Java

-   **Platform:** Android (Native)

-   **Database:** SQLite

-   **UI Components:** AndroidX, Material Components, `RecyclerView`

-   **Image Loading:** Glide

-   **Build System:** Gradle

Getting Started
---------------

Ready to run the app yourself? Here's how.

### Prerequisites

-   Android Studio (latest version recommended)

-   An Android Emulator or a physical Android device

### Setup Instructions

1.  **Clone the repository:**

    ```
    git clone https://github.com/AlMamun09/EasyShop-Android-App.git
    ```

2.  **Open in Android Studio:**

    -   Launch Android Studio.

    -   Select `File` > `Open...`

    -   Navigate to the cloned directory and select it.

3.  **Build the Project:**

    -   Wait for Android Studio to sync the Gradle files and download all the necessary dependencies.

    -   Once the sync is complete, click the `Run 'app'` button (the green play icon) in the toolbar.

4.  **Run the App:**

    -   Select your target device (emulator or physical device).

    -   The app will install and launch, starting with the login screen. You can create a new account or use one you've already made.

Future Improvements
-------------------

This app provides a solid foundation. Here are a few ideas for taking it to the next level:

-   **Implement the Shopping Cart:** Build out the functionality to add products to a cart and calculate totals.

-   **Sales History:** Activate the "Sales History" button to log transactions and provide basic sales reports.

-   **Search and Filter:** Add a real-time search bar to filter the product list on the home screen.

-   **Room Database:** Migrate from the standard `SQLiteOpenHelper` to the **Room Persistence Library** for a more modern, robust, and less error-prone database implementation.

-   **User Profiles:** Allow users to view and update their profile information.

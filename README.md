🚀 Skill4U

A modern platform designed to connect learners with skill-based resources and opportunities.

📌 Features

🔍 Browse & Search skills or learning paths

👥 User Authentication & Profiles

📚 Resource Sharing – share courses, videos, documents as messages or links

⭐ Save & Track Progress

📊 Dashboard & Analytics

🛠️ Tech Stack

Frontend: XML (Android UI)

Backend: Java

Database: Firebase Realtime Database

📂 Project Structure
skill4u/
│
├── app/                     # Android application code
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/        # Java source files (Activities, Fragments, Services)
│   │   │   │   └── com/
│   │   │   │       └── skill4u/
│   │   │   │           ├── auth/         # Authentication (Login, Signup)
│   │   │   │           ├── dashboard/    # Dashboard & Analytics
│   │   │   │           ├── models/       # Data models (User, Skill, Resource)
│   │   │   │           ├── resources/    # Resource sharing logic
│   │   │   │           ├── utils/        # Utility classes & helpers
│   │   │   │           └── MainActivity.java
│   │   │   │
│   │   │   ├── res/         # Android resources (XML layouts, drawables, values)
│   │   │   │   ├── layout/  # UI Layout files
│   │   │   │   ├── values/  # Strings, colors, styles
│   │   │   │   └── drawable/ # Icons & images
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── test/            # Unit & instrumented tests
│   │
│   └── build.gradle         # Gradle build configuration
│
├── docs/                    # Documentation & assets
│   └── README.md
│
├── .gitignore
├── build.gradle
└── LICENSE

🚀 Getting Started
✅ Prerequisites

Make sure you have installed:

Git

Android Studio

Java JDK 8+

🔧 Installation
# Clone the repo
git clone https://github.com/yasirunadeesha/skill4u.git
cd skill4u


Open the project in Android Studio, sync Gradle, and run on an emulator or device.

📜 License

This project is licensed under the MIT License.

✨ Built with passion by @yasirunadeesha

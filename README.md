# 🌿 LeafCheck - AI-Powered Leaf Health Detector

LeafCheck is an AI-based Android application that allows users to **analyze leaf health** using their smartphone camera or gallery images. Whether you're a **farmer, gardener, or plant enthusiast**, this app helps detect leaf conditions quickly and accurately! 🍃

---

## 🚀 Features

✅ **📸 Scan or Upload Images** – Capture a leaf using your camera or choose an image from your gallery.  
✅ **🔍 AI-Powered Analysis** – Get instant and accurate results on leaf health.  
✅ **🌱 Supports Multiple Tree Types** – Works with **Apple 🍎, Mango 🥭, and Orange 🍊 trees**.  
✅ **📊 Save & Track Your Trees** – Keep a history of previous scans for better monitoring.  
✅ **🎨 Beautiful UI Design** – A modern and intuitive interface with rounded icons and transparent frames.  
✅ **🔄 Firebase Integration** – Secure authentication and real-time Firestore database for storing user data.

---

## 📷 Screenshots
![LeafCheck Showcase](https://github.com/user-attachments/assets/29ab3178-b5fd-4bb7-972f-32227ac29308)

---

## 🛠️ Tech Stack

- **Kotlin** – Main programming language
- **Firebase Firestore** – Database for storing user trees
- **Firebase Authentication** – Secure user login
- **Android Jetpack** – RecyclerView, Navigation, LiveData, ViewModel
- **AI Model (Flask API)** – Locally deployed leaf health detection

---

## 🏗️ Project Setup

### Prerequisites
- Android Studio **Giraffe or later**
- Kotlin 1.8+
- Firebase Project with Authentication & Firestore setup

### Steps to Run the App
1. **Clone the Repository**
   ```sh
   git clone https://github.com/yourusername/LeafCheck.git
   cd LeafCheck
   ```
2. **Open in Android Studio**
3. **Run the App on Emulator or Device**


---

## 🌍 API Integration
The app communicates with a locally deployed Flask API for leaf health analysis. The API response format:
```json
{
  "status": "success",
  "leaftype": "expected_type",
  "leaf": "health_condition",
  "keterangan": "description"
}
```

---

## ✨ Future Improvements
🔹 Support for more tree types 🌳  
🔹 Improved AI accuracy with a cloud-based model ☁️  
🔹 Dark mode & UI enhancements 🌙  
🔹 User feedback & bug reporting system 🐞

---

## 🤝 Contributing
We welcome contributions! Follow these steps:
1. Fork the repository 🍴
2. Create a new branch (`feature-branch`) 🌱
3. Commit your changes (`git commit -m 'Add new feature'`) 💡
4. Push and create a pull request 📩

---

## 📧 Contact
For any questions or suggestions, feel free to reach out:

📩 Email: m.hilmi.adz@gmail.com  
🔗 LinkedIn: [MHA](www.linkedin.com/in/m-hilmi-adzkia)

---

🌿 **Try LeafCheck now and keep your plants healthy!** 🍃

# **📘 项目名称**

> 一款 **智能时间管理** 与 **语言设置** 工具，支持时间区间检测、JSON 数据处理、多语言切换等功能。

## **📌 项目介绍**
本项目提供：
- 🕒 **时间管理**：检测当前时间是否在设定的时间区间内。
- 🔄 **数据转换**：支持 JSON 数据格式处理，包含时间区间反转、键值交换等功能。
- 🌍 **多语言支持**：支持 **中文、英文、日语** 语言切换。
- 🎨 **界面调整**：可自定义 **颜色、字体大小**，提供 **浅休眠** 和 **深度休眠** 设置。

## **🚀 功能特点**
✅ **智能时间检测**（`HHmm-HHmm` 时间区间解析）  
✅ **JSON 数据处理**（键值反转、数据转换）  
✅ **多语言支持**（`Chinese`, `English`, `Japanese`）  
✅ **用户界面优化**（颜色选择、全屏模式、倒计时）  
✅ **休眠模式**（浅休眠 / 深度休眠）

## **🔧 安装与运行**
### **📥 依赖**
- `Java 8+`
- `Gson 库`（用于处理 JSON）
- `Maven`（可选，用于依赖管理）

### **📌 安装步骤**
1. **克隆项目**
   ```bash
   git clone https://github.com/your-repo/project-name.git
   cd project-name
   ```
2. **编译项目**
   ```bash
   mvn clean package
   ```
3. **运行**
   ```bash
   java -jar project-name.jar
   ```

## **📜 使用说明**
### **🔹 检测当前时间是否在时间区间**
调用 `isCurrentTimeInRange()` 方法：
```java
boolean result = isCurrentTimeInRange();
System.out.println("当前时间是否在区间内: " + result);
```

### **🔹 交换 JSON 键和值**
```java
JsonObject reversedJson = reverseJsonKeysAndValues(jsonObject);
System.out.println(reversedJson);
```

### **🔹 多语言切换**
支持 `Chinese`, `English`, `Japanese`：
```java
setLanguage("Japanese");
```

## **📂 项目结构**
```plaintext
📦 project-name
 ┣ 📂 src/main/java
 ┃ ┣ 📜 TimeChecker.java  # 时间区间检测
 ┃ ┣ 📜 JsonProcessor.java  # JSON 数据处理
 ┃ ┣ 📜 LanguageManager.java  # 语言切换
 ┣ 📂 config
 ┃ ┣ 📜 settings.json  # 项目配置
 ┣ 📜 README.md  # 说明文档
 ┣ 📜 data.json  # 示例数据
 ┣ 📜 project-name.jar  # 运行文件
```

## **📌 贡献**
欢迎大家贡献代码！请 **提交 Pull Request** 或 **提出 Issue** 😃

## **📜 许可证**
本项目采用 **MIT License** 进行开源，详情请查看 [LICENSE](LICENSE)。

---

🎉 **感谢你的使用，希望这个项目能帮助你高效管理时间！**
```
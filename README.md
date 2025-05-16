# EventSphere

| Diagram Type       | Image |
|--------------------|-------|
| **Context Diagram** | ![Context Diagram](https://github.com/user-attachments/assets/4a771df2-ba2d-4e19-9765-747f2389ec54) |
| **Container Diagram** | ![Container Diagram](https://github.com/user-attachments/assets/118ae217-5a5e-4c02-8e27-a426959735f7) |
| **Deployment Diagram** | ![Deployment Diagram](https://github.com/user-attachments/assets/08957ed9-a11b-4f73-a1d0-4692dd07dc7d) |

## After Risk Analysis

| Diagram Type         | Image |
|----------------------|-------|
| **Risk Analysis**     | ![Risk Analysis](https://github.com/user-attachments/assets/43e4e319-2e5a-4cea-b83c-c10acc673966) |
| **Context Diagram**   | ![Context Diagram](https://github.com/user-attachments/assets/0b6b3c40-6209-4dac-a96c-d5dac01387cd) |
| **Container Diagram** | ![Container Diagram](https://github.com/user-attachments/assets/be888573-8700-448e-8a96-acf38010bc59) |
| **Deployment Diagram**| ![Deployment Diagram](https://github.com/user-attachments/assets/08957ed9-a11b-4f73-a1d0-4692dd07dc7d) |

Risk Storming was implemented for the EventSphere system to proactively identify, analyze, and mitigate potential security and operational risks from the initial design stages. This collaborative technique allowed our team to systematically examine the system architecture, focusing on potential vulnerabilities related to user roles (Organizer, Admin, Guest, Attendee) and core functionalities such as event creation, ticket purchasing, and user authentication.

By scrutinizing the interactions between users and the EventSphere platform, including its Authentication Microservice, we identified critical risks such as unauthorized access to event management functions, potential data breaches of sensitive user and transaction information, and system performance issues during high-demand periods like ticket sales launches. For example, we pinpointed the need for robust authorization mechanisms to ensure that only authenticated Organizers can modify event details and that Admins have secure access to system oversight. Furthermore, we addressed potential bottlenecks in the ticket purchasing flow for Guests and Attendees to ensure a smooth and reliable user experience. This structured, risk-first approach ensures that EventSphere is developed to be not only feature-rich but also secure, resilient, and capable of handling expected user loads.

## Individual 

### Min 

| Diagram Type | Image |
|-------|------|
| Component Diagram Ticket System | ![component_diagram drawio](https://github.com/user-attachments/assets/b4f79376-69f3-4cad-acac-aba1b49c9947) |
| Code Diagram Ticket System | ![code_diagram drawio](https://github.com/user-attachments/assets/0a23e5af-58d9-4a05-8f3a-7989928115d5) |

### Adyo 

| Diagram Type | Image |
|-------|------|
| Component Diagram Event Management | ![EventManagementComponentDiagram](https://github.com/user-attachments/assets/91521f61-dae0-4823-a1c6-3322a476b71f) |
| Code Diagram Event Management | ![CodeDiagramEventManagement](https://github.com/user-attachments/assets/aca02926-dea4-4fc9-a864-ac19e4c66d0f) |

### Davin

Davin

| Diagram Type | Image |
|-------|------|
| Component Diagram PaymentandBalanceManagement | ![Component diagram drawio](https://github.com/user-attachments/assets/d9feb647-0d56-4430-aa78-ddf7c912c78e)|
| Code Diagram Admin side (Audit) |![AdminUsecase](https://github.com/user-attachments/assets/2b82511d-94b9-411d-8621-87eb10035424)|
| Code Diagram Non-Admin side (Topup and Pay) |![PaymentHandler](https://github.com/user-attachments/assets/e60337ce-b061-41ca-aead-610a294c7aa1)|

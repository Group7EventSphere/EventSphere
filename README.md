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

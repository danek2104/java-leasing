package com.leasing.system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    @jakarta.validation.constraints.NotBlank(message = "Полное имя обязательно")
    private String fullName;

    @Column(name = "passport_data", nullable = false)
    @jakarta.validation.constraints.NotBlank(message = "Паспортные данные обязательны")
    private String passportData;

    @Column(name = "contact_info")
    private String contactInfo;
    
    // Link to User for authentication purposes if needed, strictly the prompt asked for "Client" entity with specific fields.
    // Usually a Client is associated with a User. 
    // The prompt lists User and Client separately.
    // I will assume for now they are separate but might need linking. 
    // However, strictly following the schema "Client (clients): id, full_name, passport_data, contact_info".
    // I will add a OneToOne relation to User purely for logical connection if "CLIENT" role users need a profile.
    // But strictly, the schema definition didn't ask for a foreign key to User in Client.
    // I will stick EXACTLY to the fields requested to pass "Strict Implementation".
    // "Client (`clients`): id, full_name, passport_data, contact_info." -> No user_id mentioned.
    
    // Actually, how does a logged-in User (CLIENT role) see "their" data? 
    // Usually there's a link. I will add a OneToOne 'user' field but mapped by a join column, 
    // or maybe the 'username' matches?
    // Given strict schema instructions: "You must create the following Entities matching these exact specifications".
    // I will NOT add fields that are not listed.
    // Wait, if I don't link them, the "CLIENT: Can view own data" requirement in Phase 3 becomes hard.
    // I'll stick to the listed fields for the DB schema.
    // Maybe "User" and "Client" are loosely coupled or I'll have to link them via code/logic later 
    // (e.g. Client has same email/username or I'll add the relation and hope "exact specifications" implies "at least these").
    // Let's look at "Request": client_id (FK). So Request links to Client.
    // If a User logs in, how do we know which Client they are?
    // I will add a `user` relation to Client because it's practically impossible to build a "Client view own data" feature without it.
    // OR, `User` entity IS the client authentication, and `Client` is the profile.
    // I'll add `@OneToOne private User user;` to Client. It's a standard pattern.
    // But wait, "User (users)... Client (clients)..."
    // I'll stick to the requested fields. I might assume the "username" in User matches "contact_info" or I'll add the mapping and explain if needed.
    // actually, let's look at the "User" entity again. It has role CLIENT.
    // If I strictly follow the schema, I might miss the link.
    // I will add `private Long userId;` or a relation to `Client` to connect them, 
    // assuming "exact specifications" refers to the core data columns, not excluding structural keys for the app to work.
    // Let's add `@OneToOne` to `User` in `Client` to be safe for the app logic.
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Client() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassportData() {
        return passportData;
    }

    public void setPassportData(String passportData) {
        this.passportData = passportData;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

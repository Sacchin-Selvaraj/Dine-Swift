
CREATE TABLE users (

    user_id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,


    first_name VARCHAR(100) ,
    last_name VARCHAR(100) ,
    gender VARCHAR(20),
    dob DATE,
    phone_number VARCHAR(20),


    address TEXT,
    area VARCHAR(100),
    city VARCHAR(100),
    district VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    pincode VARCHAR(10),

	password_forgot_token VARCHAR(255),
    token_expiry_date TIMESTAMPTZ,

    registration_date TIMESTAMPTZ NOT NULL,
    system_updated_date TIMESTAMPTZ NOT NULL,
    last_login_time TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
	is_verified BOOLEAN,

    cart_id UUID UNIQUE -- Unique ensures one-to-one relationship


);

CREATE TABLE carts (

    cart_id UUID PRIMARY KEY,
    grand_total NUMERIC(10, 2) NOT NULL DEFAULT 0.00,

    -- Timestamps
    cart_created_at TIMESTAMPTZ NOT NULL ,
    cart_updated_at TIMESTAMPTZ NOT NULL ,
	cart_status VARCHAR(20) DEFAULT ACTIVE,

    -- Flags and Status
    is_guest_cart BOOLEAN NOT NULL DEFAULT FALSE,
	is_active BOOLEAN NOT NULL DEFAULT TRUE
);


ALTER TABLE users
ADD CONSTRAINT fk_user_cart
    FOREIGN KEY (cart_id)
    REFERENCES carts(cart_id)
    ON DELETE SET NULL;



CREATE TABLE bookings (

    booking_id UUID PRIMARY KEY,

    table_booking_id UUID NOT NULL,
    user_id UUID NOT NULL,

    booking_time TIMESTAMPTZ NOT NULL,
    booking_status VARCHAR(20),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE SET NULL
);

CREATE TABLE roles (

    role_id UUID PRIMARY KEY,

    role_name VARCHAR(50) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL ,
    last_modified_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE user_role (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    -- Define composite primary key
    PRIMARY KEY (user_id, role_id),

    -- Define foreign key constraints
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id)
        REFERENCES roles(role_id)
        ON DELETE CASCADE
);
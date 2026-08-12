create table user_accounts (
    id varchar2(36) primary key,
    username varchar2(100) not null unique,
    display_name varchar2(255) not null,
    password_hash varchar2(255) not null,
    role varchar2(20) not null,
    active number(1) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create index idx_user_accounts_role on user_accounts(role);

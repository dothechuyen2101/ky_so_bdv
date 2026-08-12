create table signed_records (
    id varchar2(36) primary key,
    source_system varchar2(40) not null,
    business_key varchar2(120) not null unique,
    customer_id varchar2(80) not null,
    customer_name varchar2(255),
    account_no varchar2(80),
    product_code varchar2(80),
    branch_code varchar2(40),
    record_type varchar2(80),
    status varchar2(40) not null,
    ingested_at timestamp not null,
    signed_at timestamp,
    payload_hash varchar2(128) not null,
    canonical_payload clob not null,
    signature_value clob,
    certificate_serial varchar2(160),
    signer varchar2(160),
    signature_timestamp varchar2(80),
    version_number number(19,0)
);

create index idx_signed_records_customer on signed_records(customer_id);
create index idx_signed_records_branch on signed_records(branch_code);
create index idx_signed_records_status on signed_records(status);
create index idx_signed_records_signed_at on signed_records(signed_at);

create table outbox_event (
    id varchar2(36) primary key,
    event_id varchar2(100) not null unique,
    source_system varchar2(50) not null,
    event_type varchar2(100) not null,
    aggregate_type varchar2(100),
    aggregate_id varchar2(100),
    transaction_id varchar2(100),
    document_type varchar2(50),
    payload clob,
    payload_hash varchar2(128),
    status varchar2(30) not null,
    retry_count number(10,0) default 0 not null,
    error_message clob,
    created_at timestamp not null,
    processed_at timestamp,
    updated_at timestamp
);

create index idx_outbox_event_status_created on outbox_event(status, created_at);
create index idx_outbox_event_transaction on outbox_event(transaction_id);

create table outbox_dead_letter (
    id varchar2(36) primary key,
    event_id varchar2(100) not null,
    source_system varchar2(50) not null,
    event_type varchar2(100) not null,
    payload clob,
    error_message clob,
    retry_count number(10,0) not null,
    created_at timestamp not null
);

create index idx_outbox_dead_letter_event on outbox_dead_letter(event_id);

create table outbox_event_archive (
    id varchar2(36) primary key,
    event_id varchar2(100) not null,
    source_system varchar2(50) not null,
    event_type varchar2(100) not null,
    status varchar2(30) not null,
    payload_hash varchar2(128),
    created_at timestamp not null,
    processed_at timestamp,
    archived_at timestamp not null
);

create table digital_document (
    id varchar2(36) primary key,
    document_code varchar2(100) not null unique,
    source_system varchar2(50) not null,
    transaction_id varchar2(100) not null,
    customer_id varchar2(100),
    customer_name varchar2(255),
    account_no_masked varchar2(50),
    document_type varchar2(50) not null,
    business_type varchar2(50),
    branch_code varchar2(50),
    amount number(19,2),
    currency varchar2(10),
    transaction_date timestamp,
    signed_at timestamp,
    status varchar2(30) not null,
    reconciliation_status varchar2(40) not null,
    version_number number(10,0) default 1 not null,
    original_file_path varchar2(500),
    signed_file_path varchar2(500),
    original_hash varchar2(128),
    signed_hash varchar2(128),
    file_size number(19,0),
    mime_type varchar2(100),
    signature_algorithm varchar2(100),
    certificate_serial varchar2(200),
    signer_subject varchar2(500),
    timestamp_token_id varchar2(200),
    data_snapshot clob,
    signature_value clob,
    created_at timestamp not null,
    updated_at timestamp not null
);

create index idx_doc_transaction_id on digital_document(transaction_id);
create index idx_doc_customer_id on digital_document(customer_id);
create index idx_doc_document_type on digital_document(document_type);
create index idx_doc_branch_code on digital_document(branch_code);
create index idx_doc_transaction_date on digital_document(transaction_date);
create index idx_doc_signed_at on digital_document(signed_at);
create index idx_doc_status on digital_document(status);
create index idx_doc_reconciliation on digital_document(reconciliation_status);
create index idx_doc_type_date on digital_document(document_type, transaction_date);
create index idx_doc_customer_date on digital_document(customer_id, transaction_date);
create index idx_doc_branch_date on digital_document(branch_code, transaction_date);

create table audit_logs (
    id varchar2(36) primary key,
    action varchar2(120) not null,
    actor varchar2(120) not null,
    target_type varchar2(80),
    target_id varchar2(120),
    message varchar2(1000),
    metadata clob,
    created_at timestamp not null
);

create index idx_audit_logs_created_at on audit_logs(created_at);
create index idx_audit_logs_action on audit_logs(action);

create table api_clients (
    id varchar2(36) primary key,
    client_id varchar2(120) not null unique,
    client_name varchar2(255) not null,
    api_key_hash varchar2(128) not null,
    roles varchar2(400) not null,
    active number(1) not null,
    created_at timestamp not null
);

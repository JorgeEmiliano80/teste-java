CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    assignment VARCHAR(50) NOT NULL
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(50) NOT NULL,
    risk_classification VARCHAR(50),
    start_date DATE,
    end_date DATE,
    budget NUMERIC(19, 2),
    manager_id BIGINT,
    CONSTRAINT fk_projects_manager
        FOREIGN KEY (manager_id)
        REFERENCES members (id)
);

CREATE TABLE project_members (
    project_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, member_id),
    CONSTRAINT fk_project_members_project
        FOREIGN KEY (project_id)
        REFERENCES projects (id),
    CONSTRAINT fk_project_members_member
        FOREIGN KEY (member_id)
        REFERENCES members (id)
);

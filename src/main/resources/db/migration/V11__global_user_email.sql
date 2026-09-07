DROP INDEX ux_app_user_email;

CREATE UNIQUE INDEX ux_app_user_email ON app_user (lower(email));

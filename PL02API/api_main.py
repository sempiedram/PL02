
import http.server
import pyswip
import json
import urllib.parse
import psycopg2
import uuid
import bcrypt
import psycopg2.sql

UTF8 = "utf-8"

prolog = pyswip.Prolog()
prolog.consult("prolog_api.pl")

postgresql_connection = psycopg2.connect("dbname=PL02DB user=postgres password=12345678")
postgresql = postgresql_connection.cursor()


# result = prolog.query("recipe_name(RecipeID, _).")
# print(list(result))


class PrologJSONEncoder(json.JSONEncoder):
    def default(self, o):
        return str(o)


def parse_request_path(request_path):
    parsed_request_path = urllib.parse.urlparse(request_path)
    path = parsed_request_path.path
    query = urllib.parse.parse_qs(parsed_request_path.query)
    return path, query


def recipes_all():
    result = list(prolog.query("all_recipes(RecipesIDs)"))[0]
    # all_recipes_ids = [recipe_id.decode(UTF8) for recipe_id in result.get("RecipesIDs")]
    return json.dumps(result)


def hash_password(password):
    return bcrypt.hashpw(password.encode(UTF8), bcrypt.gensalt()).decode(UTF8)


def JSON_encode(thing):
    return json.dumps(thing, cls=PrologJSONEncoder)


def recipe_info(recipe_id):
    result = next(prolog.query("recipe_info(\"{}\", RecipeInfo)".format(recipe_id)))
    return JSON_encode(result)


def generate_session_token():
    return uuid.uuid4()


def check_user_exists(username):
    postgresql.execute(psycopg2.sql.SQL('SELECT EXISTS(SELECT 1 FROM users."user" u WHERE u.username = %s) AS EXISTS'),
                       [username])

    postgresql_connection.commit()

    return postgresql.fetchone()[0]


def get_user_password_hash(username):
    postgresql.execute(psycopg2.sql.SQL('SELECT u.password_hash FROM users.user u WHERE u.username = %s'), [username])
    postgresql_connection.commit()
    result = postgresql.fetchone()

    if result is None:
        return None
    return result[0]


def register_user(username, hashed_password, name, email):
    postgresql.execute(psycopg2.sql.SQL("CALL users.register_user(%s, %s, %s, %s)"),
                       [username, hashed_password, name, email])

    postgresql_connection.commit()


class HTTPRequestHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        path, path_parameters = parse_request_path(self.path)
        path_parts = path.split("/")

        # print("parameters: {}".format(parameters))
        # print("type(parameters): {}".format(str(type(parameters))))

        if path == "/app/login":
            self.send_response(200)
            self.send_header("Content-Type", "text/html")
            self.end_headers()

            form_text = """
            <!DOCTYPE html>

            <html>
                <head>
                    <title>App - Login</title>
                </head>

                <body>
                    <p>App - Login</p>
                    <form action="/users/login" method="post">
                        username:
                        <input type="text" name="username"> <br>

                        password:
                        <input type="password" name="password"> <br>

                        <input type="submit" value="Submit">
                    </form>

                    <a href="./register">Register</a>
                </body>
            </html>
            """

            self.wfile.write(bytes(form_text, UTF8))
        elif path == "/app/register":
            self.send_response(200)
            self.send_header("Content-Type", "text/html")
            self.end_headers()

            form_text = """
            <!DOCTYPE html>

            <html>
                <head>
                    <title>App - Register</title>
                </head>

                <body>
                    <p>App - Register</p>
                    <form action="/users/register" method="post">
                        username:
                        <input type="text" name="username"> <br>

                        email:
                        <input type="text" name="email"> <br>

                        name:
                        <input type="text" name="name"> <br>

                        password:
                        <input type="password" name="password"> <br>

                        confirm password:
                        <input type="password" name="confirm_password"> <br>

                        <input type="submit" value="Submit">
                    </form>

                    <a href="./login">Log In</a>
                </body>
            </html>
            """

            self.wfile.write(bytes(form_text, UTF8))
        else:
            if path_parts[0] == "":
                if path_parts[1] == "recipes":
                    if path_parts[2] == "all":
                        self.send_response(200)
                        self.send_header("Content-Type", "application/json")
                        self.end_headers()

                        result = recipes_all()
                        self.wfile.write(result.encode(UTF8))
                        return
                    if path_parts[2] == "all_info":
                        self.send_response(200)
                        self.send_header("Content-Type", "application/json")
                        self.end_headers()

                        recipe_id = path_parameters["recipe_id"][0]

                        print("recipe_id: {}".format(recipe_id))

                        result = recipe_info(recipe_id)
                        self.wfile.write(result.encode(UTF8))
                        return
                    if path_parts[2] == "photo":
                        self.send_response(200)
                        self.send_header("Content-Type", "application/json")
                        self.end_headers()

                        recipe_id = path_parameters["recipe_id"][0]

                        print("recipe_id: {}".format(recipe_id))

                        result = recipe_info(recipe_id)
                        self.wfile.write(result.encode(UTF8))
                        return

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()

            self.wfile.write(bytes("Unknown API endpoint.\n", UTF8))
            self.wfile.write(bytes(str(self.client_address) + "\n", UTF8))
            self.wfile.write(bytes(str(self.path) + "\n", UTF8))
            self.wfile.write(bytes(str(path) + "\n", UTF8))
            self.wfile.write(bytes(str(path_parts) + "\n", UTF8))
            self.wfile.write(bytes(str(path_parameters) + "\n", UTF8))

    def do_POST(self):
        path, path_parameters = parse_request_path(self.path)
        path_parts = path.split("/")

        content_length = int(self.headers["Content-Length"])
        body = (self.rfile.read(content_length)).decode(UTF8)
        body_parameters = urllib.parse.parse_qs(body)

        if path_parts[1] == "users":
            if path_parts[2] == "login":
                username = body_parameters.get("username")[0]
                password = body_parameters.get("password")[0]

                print("username: {}, password: {}".format(username, password))

                user_exists = check_user_exists(username)

                if user_exists:
                    db_hashed_password = get_user_password_hash(username)

                    if bcrypt.checkpw(password.encode(UTF8), db_hashed_password.encode(UTF8)):
                        self.send_response(200)
                        self.end_headers()

                        success_result = {"outcome": "success",
                                          "session_token": generate_session_token()}

                        self.write_json(success_result)
                        return
                    else:
                        self.send_response(202)
                        self.end_headers()

                        error_result = {"outcome": "error",
                                        "error": "incorrect_login_info",
                                        "error_msg": "Incorrect password or username."}

                        self.write_json(error_result)
                        return
                else:
                    self.send_response(202)
                    self.end_headers()

                    error_result = {"outcome": "error",
                                    "error": "incorrect_login_info",
                                    "error_msg": "Incorrect password or username."}

                    self.write_json(error_result)
                    return
                return
            if path_parts[2] == "register":
                username = body_parameters.get("username")[0]
                name = body_parameters.get("name")[0]
                email = body_parameters.get("email")[0]
                password = body_parameters.get("password")[0]
                confirm_password = body_parameters.get("confirm_password")[0]

                if password != confirm_password:
                    self.send_response(202)
                    self.end_headers()

                    error_result = {"outcome": "error",
                                    "error": "passwords_dont_match",
                                    "error_msg": "The password and the confirmation password doesn't match."}

                    self.write_json(error_result)
                    return

                print("username: '{}', name: '{}', email: '{}', password: '{}', confirm_password: '{}'".format(
                    username, name, email, password, confirm_password))

                hashed_password = hash_password(password)

                register_user(username, hashed_password, name, email)

                self.send_response(200)
                self.end_headers()

                response = {"success": "user_registered"}

                self.write_json(response)

                return

        self.send_response(202)
        self.end_headers()

        error_result = {"error": "unknown_api_endpoint", "error_msg": "Unknown API endpoint: {}.".format(self.path)}

        self.wfile.write(bytes(json.dumps(error_result), UTF8))
        return

    def write_json(self, to_send):
        self.wfile.write(JSON_encode(to_send).encode(UTF8))


server_address = ("", 5000)
http_server = http.server.HTTPServer(server_address, HTTPRequestHandler)
http_server.serve_forever()

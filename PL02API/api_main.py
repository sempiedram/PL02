
import http.server
import pyswip
import json
import urllib.parse
import psycopg2
import uuid
import bcrypt
import psycopg2.sql
import psycopg2.errors

UTF8 = "utf-8"

prolog = pyswip.Prolog()
prolog.consult("prolog_api.pl")

postgresql_connection = psycopg2.connect("dbname=postgres user=postgres password=12345678")
postgresql = postgresql_connection.cursor()


# result = prolog.query("recipe_name(RecipeID, _).")
# print(list(result))


class PrologJSONEncoder(json.JSONEncoder):
    def default(self, o):
        if type(o) == bytes:
            return o.decode(UTF8)
        return str(o)


def parse_request_path(request_path):
    parsed_request_path = urllib.parse.urlparse(request_path)
    path = parsed_request_path.path
    query = urllib.parse.parse_qs(parsed_request_path.query)
    return path, query


def recipes_all():
    """
    Returns the ids of all recipes.
    :return: A list with all the ids.
    """
    result = list(prolog.query("all_recipes(RecipesIDs)"))[0]["RecipesIDs"]
    return result


def hash_password(password):
    return bcrypt.hashpw(password.encode(UTF8), bcrypt.gensalt()).decode(UTF8)


def json_encode(thing):
    return json.dumps(thing, cls=PrologJSONEncoder)


def get_first_value_from_dict(dict_):
    keys = list(dict_.keys())

    if len(keys) < 1:
        return None

    return dict_[keys[0]]


def recipe_info(recipe_id):
    prolog_result = next(prolog.query("recipe_info(\"{}\", RecipeInfo)".format(curate_prolog_text(recipe_id))))
    print("PROLOG RESULT : {}".format(prolog_result))
    # TODO: What happens when the recipe doesn't exist.

    # recipe_type = next(prolog.query("recipe_type(\"{}\", RecipeType)".format(curate_prolog_text(recipe_id))))

    recipe_type = prolog_query("recipe_type", recipe_id, "RecipeType")
    if len(recipe_type) < 1:
        recipe_type = None
    else:
        recipe_type = get_first_value_from_dict(recipe_type[0])

    recipe_ingredients = prolog_query("recipe_ingredients", recipe_id, "RecipeIngredients")
    if len(recipe_ingredients) < 1:
        recipe_ingredients = None
    else:
        recipe_ingredients = get_first_value_from_dict(recipe_ingredients[0])

    # TODO: Test what happens when there are no steps.
    recipe_steps = prolog_query("recipe_steps", recipe_id, "RecipeSteps")
    if len(recipe_steps) < 1:
        recipe_steps = None
    else:
        recipe_steps = get_first_value_from_dict(recipe_steps[0])

        for recipe_step in recipe_steps:
            recipe_step[1] = str(recipe_step[1])

    recipe_photos = prolog_query("recipe_photographs", recipe_id, "RecipePhotographs")
    if len(recipe_photos) < 1:
        recipe_photos = None
    else:
        recipe_photos = get_first_value_from_dict(recipe_photos[0])

    result = {"id": recipe_id,
              "type": recipe_type,
              "ingredients": recipe_ingredients,
              "steps": recipe_steps,
              "photos": recipe_photos}

    print("Result ::: {}".format(result))

    return result


def generate_session_token():
    return str(uuid.uuid4())


def check_user_exists(username):
    postgresql.execute(psycopg2.sql.SQL('SELECT EXISTS(SELECT 1 FROM users."user" u WHERE u.username = %s) AS EXISTS'),
                       [username])

    postgresql_connection.commit()
    # TODO: Rollback on errors!

    return postgresql.fetchone()[0]


def get_user_password_hash(username):
    postgresql.execute(psycopg2.sql.SQL('SELECT u.password_hash FROM users.user u WHERE u.username = %s'), [username])
    postgresql_connection.commit()
    result = postgresql.fetchone()

    # TODO: Rollback on errors!

    if result is None:
        return None
    return result[0]


def register_user(username, hashed_password, name, email):
    try:  # TODO: Return what happened.
        postgresql.execute(psycopg2.sql.SQL("CALL users.register_user_sp(%s, %s, %s, %s)"),
                           [username, hashed_password, name, email])

        postgresql_connection.commit()
        return True
    except psycopg2.errors.UniqueViolation:
        postgresql_connection.rollback()
    return False


def curate_prolog_text(text):
    valid_characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!.-,;:_? "
    result = ""
    for c in text:
        if c not in valid_characters:
            result += "?"
        else:
            result += c
    return result


def prolog_assert(predicate, *args):
    expanded_arguments = ", ".join(["'{}'".format(argument) for argument in args])
    command = "{}({})".format(predicate, expanded_arguments)
    print("prolog_assert: {}".format(command))
    prolog.assertz(command)


def prolog_format_argument(argument):
    argument = curate_prolog_text(argument)
    if len(argument) == 0:
        return ""
    elif argument[0].isupper():
        return argument
    return '"{}"'.format(argument)


def prolog_query(predicate, *args):
    expanded_arguments = ", ".join([prolog_format_argument(argument) for argument in args])
    command = "{}({})".format(predicate, expanded_arguments)
    result = list(prolog.query(command))
    print("prolog_query: \"\"\"{}\"\"\", result: {}".format(command, result))
    return result


def register_new_recipe(recipe_id, recipe_type, recipe_ingredients, recipe_steps, recipe_photos):
    # result = next(prolog.query("recipe_info(\"{}\", RecipeInfo)".format(curate_prolog_text(recipe_id))))
    # return json_encode(result.decode(UTF8))

    # recipes_types = list(prolog.query("recipe_type(\"{}\", RecipeID)".format(curate_prolog_text(recipe_id))))
    recipes_types = prolog_query("recipe_type", recipe_id, "RecipeID")

    if len(recipes_types) == 0:
        prolog_assert("recipe_type", recipe_type)

        for ingredient in recipe_ingredients:
            prolog_assert("recipe_ingredient", ingredient)

        for step in recipe_steps:
            prolog_assert("recipe_step", step)

        for photo in recipe_photos:
            prolog_assert("recipe_photograph", photo)
    else:
        # Recipe already exists.
        return


def check_valid_session_token(session_token):
    postgresql.execute(psycopg2.sql.SQL(
        "SELECT * FROM users.check_session_token_sp(session_token_ := %s)"), [session_token])
    postgresql_connection.commit()
    # TODO: Rollback on errors!

    return postgresql.fetchone()[0]


def save_session_token(username, session_token):
    try:
        postgresql.execute(psycopg2.sql.SQL("CALL users.add_user_session_sp(%s, %s)"),
                           [username, session_token])
        postgresql_connection.commit()
    except psycopg2.errors.UniqueViolation:
        print("Should never happen: session token {} already exists in the database.".format(session_token))
        postgresql_connection.rollback()
        # assert(False)


def logout_session(session_token):
    postgresql.execute(psycopg2.sql.SQL("CALL users.logout_session_sp(%s)"),
                       [session_token])
    postgresql_connection.commit()
    return True  # TODO: Actually return if the session_token existed.


def check_password(password, hashed_password):
    return bcrypt.checkpw(password.encode(UTF8), hashed_password.encode(UTF8))


class HTTPRequestHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        path, path_parameters = parse_request_path(self.path)
        path_parts = path.split("/")

        session_token = self.headers["Authorization"]

        if session_token is None:
            print("Authorization header not found.")
            send_error_response(self, 400, "missing_authentication", "No session token provided")
            return

        if not check_valid_session_token(session_token):
            print("handle GET: ERROR: invalid session_token")
            send_error_response(self, 400, "unauthorized", "Invalid session token.")
            return

        if path_parts[0] == "":
            if path_parts[1] == "recipes":
                if path_parts[2] == "all":
                    handle_recipes_all(self)
                    return

                if path_parts[2] == "get":
                    recipe_ids = None
                    try:
                        recipe_ids = path_parameters["recipe_id"]
                    except KeyError:
                        send_error_response(self, 400, "no_recipe_id", "No recipe id was provided.")
                        return

                    if recipe_ids is None or len(recipe_ids) == 0:
                        send_error_response(self, 400, "no_recipe_id", "No recipe id was provided.")
                        return

                    handle_recipes_get(self, recipe_ids[0])
                    return

            if path_parts[1] == "photos":
                if path_parts[2] == "get":
                    photo_ids = None
                    try:
                        photo_ids = path_parameters["photo_id"]
                    except KeyError:
                        send_error_response(self, 400, "no_photo_id", "No photo id was provided.")
                        return

                    if photo_ids is None or len(photo_ids) == 0:
                        send_error_response(self, 400, "no_photo_id", "No photo id was provided.")
                        return

                    handle_photos_get(self, photo_ids[0])
                    return

        # Handle requests that were not recognized:
        response = {"client_address": self.client_address,
                    "path": path,
                    "path_parts": path_parts,
                    "path_parameters": path_parameters}

        send_error_response(self, 400, "unknown_api_endpoint", "Unknown API endpoint.", response)

    def do_POST(self):
        path, path_parameters = parse_request_path(self.path)
        path_parts = path.split("/")

        content_length = int(self.headers["Content-Length"])
        binary_body = self.rfile.read(content_length)
        body = binary_body.decode(UTF8)

        session_token = self.headers["Authorization"]

        # TODO: Use JSON for all received parameters.

        if path_parts[1] == "users":
            if path_parts[2] == "login":
                handle_login(self, body)
                return

            if path_parts[2] == "register":
                handle_register(self, body)
                return

            if path_parts[2] == "logout":
                handle_logout(self, session_token)
                return

        if path_parts[1] == "photos":
            if path_parts[2] == "new":
                handle_photos_new(self, session_token, binary_body)
                return

        if path_parts[1] == "recipes":
            if path_parts[2] == "new":
                handle_recipes_new(self, session_token, body)
                return

        # Handle requests that were not recognized:
        response = {"client_address": self.client_address,
                    "path": path,
                    "path_parts": path_parts,
                    "path_parameters": path_parameters}

        send_error_response(self, 400, "unknown_api_endpoint", "Unknown API endpoint.", response)

    def write_json(self, to_send):
        to_send = json_encode(to_send).encode(UTF8)
        print("Sending {} bytes.".format(len(to_send)))
        self.wfile.write(to_send)


def handle_register(handler, body):
    try:
        body_parameters = json.loads(body)

        print("handle_register, body_parameters: {}".format(body_parameters))

        username = body_parameters["username"]
        name = body_parameters["name"]
        email = body_parameters["email"]
        password = body_parameters["password"]

        hashed_password = hash_password(password)
        result = register_user(username, hashed_password, name, email)

        if result:
            send_success_response(handler, "user_registered")
        else:
            # TODO: Actually say what went wrong.
            send_error_response(handler, 400, "user_already_exists", "Username is already used.")
    except KeyError as e:
        print("handle_register: KeyError: ERROR: '{}'".format(e))
        send_error_response(handler, 400, "missing_parameter", str(e))
        return
    except json.decoder.JSONDecodeError as e:
        print("handle_register: JSONDecodeError: ERROR: '{}'".format(e))
        send_error_response(handler, 400, "invalid_request", str(e))
    return


def handle_recipes_new(handler, session_token, body):
    try:
        body_parameters = json.loads(body)

        print("handle_recipes_new, body_parameters: {}".format(body_parameters))

        if not check_valid_session_token(session_token):
            print("handle_recipes_new: ERROR: invalid session_token")
            send_error_response(handler, 400, "unauthorized", "Invalid session token.")
            return

        print("handle_recipes_new: valid session_token")

        recipe_id = body_parameters["id"]
        recipe_type = body_parameters["type"]
        recipe_ingredients = body_parameters["ingredients"]
        recipe_steps = body_parameters["steps"]
        recipe_photos = body_parameters["photos"]

        register_new_recipe(recipe_id, recipe_type, recipe_ingredients, recipe_steps, recipe_photos)

        send_success_response(handler, "recipe_registered")
        return
    except KeyError as e:
        print("handle_recipes_new: KeyError: ERROR: '{}'".format(e))
        send_error_response(handler, 400, "missing_parameter", str(e))
        return
    except json.decoder.JSONDecodeError as e:
        print("handle_recipes_new: JSONDecodeError: ERROR: '{}'".format(e))
        send_error_response(handler, 400, "invalid_request", str(e))
    return


def handle_photos_new(handler, session_token, photo_data):
    if not check_valid_session_token(session_token):
        print("handle_photos_new: ERROR: invalid session_token")
        send_error_response(handler, 400, "unauthorized", "Invalid session token.")
        return

    photo_id = generate_new_photo_id()
    save_photo(photo_id, photo_data)

    result = {"photo_id": photo_id}

    send_success_response(handler, "photo_saved", result)


def handle_recipes_all(handler):
    result = {"recipes_ids": recipes_all()}
    send_success_response(handler, "recipes_all", result)


def handle_photos_get(handler, photo_id):
    photo = photos_get(photo_id)

    if photo is None:
        send_error_response(handler, 404, "photo_not_found", "Photo with id '{}' was not found.".format(photo_id))
        return

    handler.send_response(200)
    # handler.send_header("Content-Type", "image/png")
    # handler.send_header("Content-Length", len(recipe_photo))
    handler.end_headers()
    handler.wfile.write(photo)


def save_photo(photo_id, photo_data):
    photo_file = open("photos/" + photo_id + ".jpg", "wb")
    photo_file.write(photo_data)


def generate_new_photo_id():
    return str(uuid.uuid4())


def photos_get(photo_id):
    print("Loading photo '{}'.".format(photo_id))
    file_path = "photos/" + photo_id + ".jpg"
    try:
        photo_file = open(file_path, "rb")
        return photo_file.read()
    except OSError:
        print("photos_get: ERROR: Could not find file '{}'.".format(file_path))
    return None


def handle_recipes_get(handler, recipe_id):
    print("handle_recipes_get, recipe_id: {}".format(recipe_id))

    recipe = recipe_info(recipe_id)
    result = {"recipe": recipe}

    # handler.send_header("Content-Type", "application/json")
    send_success_response(handler, "recipes_get", result)


def handle_logout(handler, session_token):
    result = logout_session(session_token)

    print("Logout: {}".format(session_token))

    if result:
        send_success_response(handler, "user_logged_out", result)
    else:
        print("handle_logout: ERROR: Could not log out.")
        send_error_response(handler, 400, "token_not_found", "Could not log out.")
    return


def curate_password(password):
    non_zero = lambda x: (1 if x == 0 else x)
    return "".join([non_zero(c) for c in password])


def handle_login(handler, body):
    try:
        body_parameters = json.loads(body)

        print("handle_login, body_parameters: {}".format(body_parameters))

        username = body_parameters["username"]
        password = curate_password(body_parameters["password"])

        if check_user_exists(username):
            db_hashed_password = get_user_password_hash(username)

            if check_password(password, db_hashed_password):
                session_token = generate_session_token()
                save_session_token(username, session_token)

                result = {"session_token": session_token}
                send_success_response(handler, "user_logged_in", result)
                return

        send_error_response(handler, 400, "incorrect_login_info", "Incorrect password or username.")
        return
    except KeyError as e:
        print("handle_login: KeyError: ERROR: '{}'".format(e))
        send_error_response(handler, 400, "missing_parameter", str(e))
        return
    except json.decoder.JSONDecodeError as e:
        print("handle_login: JSONDecodeError: ERROR: '{}'".format(e))
        send_error_response(handler, 400, "invalid_request", str(e))
    return


def send_error_response(handler, http_code, error_code, error_msg, parameters=None):
    if parameters is None:
        parameters = {}

    handler.send_response(http_code)
    handler.end_headers()

    # handler.send_header("Content-Type", "application/json")

    response = {"outcome": "error",
                "error": error_code,
                "error_msg": str(error_msg)}

    response.update(parameters)

    print("Error: " + str(response))

    handler.write_json(response)


def send_success_response(handler, success_code, parameters=None):
    if parameters is None:
        parameters = {}

    handler.send_response(200)
    handler.end_headers()

    # handler.send_header("Content-Type", "application/json")

    response = {"outcome": "success",
                "success": success_code}

    response.update(parameters)

    handler.write_json(response)


if __name__ == "__main__":
    server_port = 35000
    print("Starting server at port {}.".format(server_port))
    server_address = ("", server_port)
    http_server = http.server.HTTPServer(server_address, HTTPRequestHandler)
    http_server.serve_forever()

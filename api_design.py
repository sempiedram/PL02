
# API prototypes

def account_create(username, email, password):
	# Maybe password_hash instead of password?
	return success or error

def account_login(username, password):
	return session_token or error

def account_logout(session_token):
	# Maybe username instead of session_token
	return success or error

def recipe_list_all(session_token):
	return recipe_id_list or error

def recipe_get(session_token, recipe_id):
	return recipe_details

def recipe_search_by_name(session_token, recipe_name):
	return recipe_id_list or error

def recipe_search_by_food_type(session_token, food_type):
	return recipe_id_list or error

def recipe_search_by_ingredient(session_token, ingredient):
	return recipe_id_list or error

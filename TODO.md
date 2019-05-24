
# TODO

## Main TODO

- API
	- Recipes
		- Write recipes queries
	- Database
		- Create the database diagram
		- Write database scripts
- Recipes
- App
	- Write the app
- Set up installation on the cloud

## Secondary TODO

- Set up domain for the API


- Can recipes be stored as
	
	recipeType(RecipeID, Type).
	recipeIngredient(RecipeID, Ingredient).

# Notes
- Recipes must be persistent, even after a shutdown of the virtual machine. They can be saved via Python.
- User passwords should be hashed before being stored. Preferably salted.


# Questions

- Is it necessary to use AWS or Heroku? May I use my own hosting?
	- It's necessary to use AWS/Heroku.
- Where it says 'native Android', does it mean using native binary executables, or just normal Java APKs?
	- Better use Java.
- Can I use the pyswip library for the Prolog knowledge database?
	- Yes.
- Must everything be sent in Json format?
	- Not necessarily, but better use that.
- Can I use Kotlin or other programming languages?
	- Yes, but better use Java.
- Can recipes be stored like this?:

'''
	recipeType(RecipeID, Type).
	recipeIngredient(RecipeID, Ingredient).
'''


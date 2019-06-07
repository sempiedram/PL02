
%======================= Disable some warnings =======================%

:- discontiguous recipe_step/3.
:- discontiguous recipe_name/2.
:- discontiguous recipe_type/2.
:- discontiguous recipe_ingredient/2.
:- discontiguous recipe_photograph/2.

%======================= Make the recipe rules dynamic =======================%

:- dynamic recipe_type/2.
:- dynamic recipe_ingredient/2.
:- dynamic recipe_step/3.
:- dynamic recipe_photograph/2.

%=============================== Recipe rules ===============================%


recipe_ingredients(RecipeID, RecipeIngredients) :-
	findall(Ingredient, recipe_ingredient(RecipeID, Ingredient), RecipeIngredients).

recipe_steps_unsorted(RecipeID, RecipeStepsUnsorted) :-
	findall([N, Step], recipe_step(RecipeID, N, Step), RecipeStepsUnsorted).

sort_recipe_steps(RecipeStepsUnsorted, SortedSteps) :-
	sort(RecipeStepsUnsorted, SortedSteps).

recipe_steps(RecipeID, Steps) :-
	recipe_steps_unsorted(RecipeID, UnsortedSteps),
	sort_recipe_steps(UnsortedSteps, Steps).

recipe_nth_step(RecipeID, N, Step) :- recipe_step(RecipeID, N, Step).

recipe_photographs(RecipeID, RecipePhotographs) :-
	findall(Photograph, recipe_photograph(RecipeID, Photograph), RecipePhotographs).

recipe(RecipeID, RecipeType, RecipeIngredients, RecipeSteps, RecipePhotographs) :-
	recipe_type(RecipeID, RecipeType),
	recipe_ingredients(RecipeID, RecipeIngredients),
	recipe_steps(RecipeID, RecipeSteps),
	recipe_photographs(RecipeID, RecipePhotographs).

recipe_info(RecipeID, [RecipeType, RecipeIngredients, RecipeSteps, RecipePhotographs]) :-
	recipe(RecipeID, RecipeType, RecipeIngredients, RecipeSteps, RecipePhotographs).

all_recipes(RecipesIDs) :-
	findall(RecipeID, recipe_type(RecipeID, _), RecipesIDs).


%=============================== Filtering rules ===============================%

recipe_filtered_type(RecipeID, Type) :-
	recipe_type(RecipeID, Type).
	
recipe_filtered_id(RecipeID, RecipeIDFilter) :-
	recipe_type(RecipeID, _),
	RecipeID = RecipeIDFilter.

recipe_filtered_ingredient(RecipeID, RecipeIngredient) :-
	recipe_type(RecipeID, _),
	recipe_ingredient(RecipeID, RecipeIngredient).

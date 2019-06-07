
:- discontiguous recipe_step/3.
:- discontiguous recipe_name/2.
:- discontiguous recipe_type/2.
:- discontiguous recipe_ingredient/2.
:- discontiguous recipe_photograph/2.

%=============================== Test recipes ===============================%

%== Cereal bowl ==%
    recipe_type("cereal_bowl", "quick").

    recipe_ingredient("cereal_bowl", "cereal").
    recipe_ingredient("cereal_bowl", "milk").

    recipe_photograph("cereal_bowl", "3f51ead2-550c-4faf-9439-dad9448d7897").
    recipe_photograph("cereal_bowl", "57822c08-cc9e-4348-8e21-e0cc1b158f48").
    recipe_photograph("cereal_bowl", "96df0f31-5f67-471d-8e73-57a85e4f5045").

    recipe_step("cereal_bowl", 2, 'Pour milk on the bowl.').
    recipe_step("cereal_bowl", 3, 'Enjoy.').
    recipe_step("cereal_bowl", 1, 'Add cereal to the bowl.').

%== Chocolate Drink ==%
    recipe_type("chocolate_drink", "tasty").

    recipe_ingredient("chocolate_drink", "chocolate").
    recipe_ingredient("chocolate_drink", "milk").

    recipe_photograph("chocolate_drink", "4e551886-34d1-422c-9e01-42b6daf6786a").
    recipe_photograph("chocolate_drink", "9b9c76d7-4944-4f55-93ab-869a124d4e4c").
    recipe_photograph("chocolate_drink", "cd1224f8-af42-4d74-8778-9a014c23ffc1").

    recipe_step("chocolate_drink", 1, '.').
    recipe_step("chocolate_drink", 2, 'Pour milk on the bowl.').
    recipe_step("chocolate_drink", 3, 'Enjoy.').


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

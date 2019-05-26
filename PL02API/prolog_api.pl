
:- discontiguous recipe_step/3.
:- discontiguous recipe_name/2.
:- discontiguous recipe_type/2.
:- discontiguous recipe_ingredient/2.
:- discontiguous recipe_photograph/2.

%=============================== Specific recipes ===============================%

%== Cereal bowl ==%

    recipe_name("cereal_bowl", "Cereal Bowl").

    recipe_type("cereal_bowl", "quick").

    recipe_ingredient("cereal_bowl", "cereal").
    recipe_ingredient("cereal_bowl", "milk").

    recipe_photograph("cereal_bowl", "cereal_00").
    recipe_photograph("cereal_bowl", "cereal_01").
    recipe_photograph("cereal_bowl", "cereal_02").

    recipe_step("cereal_bowl", 2, 'Pour milk on the bowl.').
    recipe_step("cereal_bowl", 3, 'Enjoy.').
    recipe_step("cereal_bowl", 1, 'Add cereal to the bowl.').

%== Chocolate Drink ==%

    recipe_name("chocolate_drink", "Chocolate Drink").

    recipe_type("chocolate_drink", "tasty").

    recipe_ingredient("chocolate_drink", "chocolate").
    recipe_ingredient("chocolate_drink", "milk").

    recipe_photograph("chocolate_drink", "chocolate_00").
    recipe_photograph("chocolate_drink", "chocolate_01").
    recipe_photograph("chocolate_drink", "chocolate_02").

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
	findall(RecipeID, recipe_name(RecipeID, _), RecipesIDs).

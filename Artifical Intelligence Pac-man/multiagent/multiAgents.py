# multiAgents.py
# --------------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
# 
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


from pickle import TRUE
from re import L
from xmlrpc.client import Boolean
from util import manhattanDistance
from game import Directions
import random, util

from game import Agent
from pacman import GameState

class ReflexAgent(Agent):
    """
    A reflex agent chooses an action at each choice point by examining
    its alternatives via a state evaluation function.

    The code below is provided as a guide.  You are welcome to change
    it in any way you see fit, so long as you don't touch our method
    headers.
    """


    def getAction(self, gameState: GameState):
        """
        You do not need to change this method, but you're welcome to.

        getAction chooses among the best options according to the evaluation function.

        Just like in the previous project, getAction takes a GameState and returns
        some Directions.X for some X in the set {NORTH, SOUTH, WEST, EAST, STOP}
        """
        # Collect legal moves and successor states
        legalMoves = gameState.getLegalActions()

        # Choose one of the best actions
        scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
        bestScore = max(scores)
        bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
        chosenIndex = random.choice(bestIndices) # Pick randomly among the best

        "Add more of your code here if you want to"

        return legalMoves[chosenIndex]

    def evaluationFunction(self, currentGameState: GameState, action):
        """
        Design a better evaluation function here.

        The evaluation function takes in the current and proposed successor
        GameStates (pacman.py) and returns a number, where higher numbers are better.

        The code below extracts some useful information from the state, like the
        remaining food (newFood) and Pacman position after moving (newPos).
        newScaredTimes holds the number of moves that each ghost will remain
        scared because of Pacman having eaten a power pellet.

        Print out these variables to see what you're getting, then combine them
        to create a masterful evaluation function.
        """
        # Useful information you can extract from a GameState (pacman.py)
        successorGameState = currentGameState.generatePacmanSuccessor(action)
        newPos = successorGameState.getPacmanPosition()
        newFood = successorGameState.getFood()
        newGhostStates = successorGameState.getGhostStates()
        newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]
        ghostpos = successorGameState.getGhostPosition(1)
        
        distbtwnghost = manhattanDistance(newPos, ghostpos)
        if distbtwnghost < 2:
            return -99999999999999
        
        mean_food = 0
        for food in newFood.asList():
            mean_food += manhattanDistance(newPos, food)
        
        if len(newFood.asList()) != 0:
            mean_food = mean_food/len(newFood.asList())
        else:
            return successorGameState.getScore()*0.2
        
        capsule_dist = 0
        for cap in successorGameState.getCapsules():
            capsule_dist = manhattanDistance(newPos, cap)
        
        mean_food = mean_food + capsule_dist
        
        #print("DEBUG")
        for food in newFood.asList():
            if food == newPos:
                print(True)
        

        return successorGameState.getScore()*0.2 + 1/mean_food

def scoreEvaluationFunction(currentGameState: GameState):
    """
    This default evaluation function just returns the score of the state.
    The score is the same one displayed in the Pacman GUI.

    This evaluation function is meant for use with adversarial search agents
    (not reflex agents).
    """
    return currentGameState.getScore()

class MultiAgentSearchAgent(Agent):
    """
    This class provides some common elements to all of your
    multi-agent searchers.  Any methods defined here will be available
    to the MinimaxPacmanAgent, AlphaBetaPacmanAgent & ExpectimaxPacmanAgent.

    You *do not* need to make any changes here, but you can if you want to
    add functionality to all your adversarial search agents.  Please do not
    remove anything, however.

    Note: this is an abstract class: one that should not be instantiated.  It's
    only partially specified, and designed to be extended.  Agent (game.py)
    is another abstract class.
    """

    def __init__(self, evalFn = 'scoreEvaluationFunction', depth = '2'):
        self.index = 0 # Pacman is always agent index 0
        self.evaluationFunction = util.lookup(evalFn, globals())
        self.depth = int(depth)

class MinimaxAgent(MultiAgentSearchAgent):
    """
    Your minimax agent (question 2)
    """

    def getAction(self, gameState: GameState):
        """
        Returns the minimax action from the current gameState using self.depth
        and self.evaluationFunction.

        Here are some method calls that might be useful when implementing minimax.

        gameState.getLegalActions(agentIndex):
        Returns a list of legal actions for an agent
        agentIndex=0 means Pacman, ghosts are >= 1

        gameState.generateSuccessor(agentIndex, action):
        Returns the successor game state after an agent takes an action

        gameState.getNumAgents():
        Returns the total number of agents in the game

        gameState.isWin():
        Returns whether or not the game state is a winning state

        gameState.isLose():
        Returns whether or not the game state is a losing state
        """
        
        scores = {}
        agents = gameState.getNumAgents()
        
        def minimax(gs: GameState, depth, is_pac_bool, evaluationfunc, num_ghosts, ghost_i,  record_value):
            
            if gs.isLose() or gs.isWin():
                return evaluationfunc(gs)
            
            if depth == 0 and is_pac_bool:
                return evaluationfunc(gs)
            
            if is_pac_bool:
                max_eval = -999999
                for child in gs.getLegalActions(0):
                    childGameState = gs.generateSuccessor(0, child)
                    curr_eval = minimax(childGameState, depth, False, evaluationfunc, num_ghosts, 1, False)
                    max_eval = max(max_eval, curr_eval)
                    if record_value:
                        scores[child] = curr_eval
                return max_eval
            
            else:
                min_eval = 999999
                ghost = ghost_i
                ghostlegalmoves = gs.getLegalActions(ghost)
                
                if num_ghosts > ghost:
                    for move in ghostlegalmoves:
                        ghost_gs = gs.generateSuccessor(ghost, move)
                        curr_eval = minimax(ghost_gs, depth, False, evaluationfunc, num_ghosts, ghost + 1,  False)
                        min_eval = min(min_eval, curr_eval)
                else:
                    for move in ghostlegalmoves:
                        ghost_gs = gs.generateSuccessor(ghost, move)
                        curr_eval = minimax(ghost_gs, depth - 1, True, evaluationfunc, agents - 1, 1, False)
                        min_eval = min(min_eval, curr_eval)
                
                    
                return min_eval
        
        final_eval = minimax(gameState, self.depth, True, self.evaluationFunction, agents - 1, 1, True)
        

        for key, value in scores.items():
            if value == final_eval:
                return key
        
        
        """
        cont = self.find_val(gameState, 0, 0)
        return cont[1]
   
    def min_val(self, gameState, index, depth):
        legalMoves = gameState.getLegalActions(index)
        min_act =  ""
        min_val = float('inf')
        for action in legalMoves:
            s = gameState.generateSuccessor(index, action)
            s_i = index + 1
            s_d = depth
            if s_i == gameState.getNumAgents():
                s_i = 0 
                s_d = s_d + 1
            newval = self.find_val(s, s_i, s_d)[0]
            if newval < min_val:
                min_val = newval
                min_act = action
        return min_val, min_act
        
    def max_val(self, gameState, index, depth):
        legalMoves = gameState.getLegalActions(index)
        max_act =  ""
        max_val = float('-inf')
        for action in legalMoves:
            s = gameState.generateSuccessor(index, action)
            s_i = index + 1
            s_d = depth
            if s_i == gameState.getNumAgents():
                s_i = 0 
                s_d = s_d + 1
            newval = self.find_val(s, s_i, s_d)[0]
            if newval > max_val:
                max_val = newval
                max_act = action
        return max_val, max_act
        
    def find_val(self,gameState,index, depth):
        find_act = ""
        if len(gameState.getLegalActions(index)) == 0:
            return gameState.getScore(), find_act
        if depth == self.depth:
            return gameState.getScore(), find_act
        if index == 0:
            return self.max_val(gameState, index, depth)
        else: 
            return self.min_val(gameState, index, depth)
    """
        
    
        
                

class AlphaBetaAgent(MultiAgentSearchAgent):
    """
    Your minimax agent with alpha-beta pruning (question 3)
    """

    def getAction(self, gameState: GameState):
        """
        Returns the minimax action using self.depth and self.evaluationFunction
        """
        scores = {}
        agents = gameState.getNumAgents()
        
        def Alphabeta(gs: GameState, depth, is_pac_bool, evaluationfunc, num_ghosts, ghost_i,  record_value, alpha, beta):
            
            if gs.isLose() or gs.isWin():
                return evaluationfunc(gs)
            
            if depth == 0 and is_pac_bool:
                return evaluationfunc(gs)
            
            if is_pac_bool:
                max_eval = -999999
                for child in gs.getLegalActions(0):
                    childGameState = gs.generateSuccessor(0, child)
                    curr_eval = Alphabeta(childGameState, depth, False, evaluationfunc, num_ghosts, 1, False, alpha, beta)
                    max_eval = max(max_eval, curr_eval)
                    if record_value:
                        scores[child] = curr_eval
                    alpha = max(alpha, curr_eval)
                    if beta < alpha:
                        break
                return max_eval
            
            else:
                min_eval = 999999
                ghost = ghost_i
                ghostlegalmoves = gs.getLegalActions(ghost)
                
                if num_ghosts > ghost:
                    for move in ghostlegalmoves:
                        ghost_gs = gs.generateSuccessor(ghost, move)
                        curr_eval = Alphabeta(ghost_gs, depth, False, evaluationfunc, num_ghosts, ghost + 1,  False, alpha, beta)
                        min_eval = min(min_eval, curr_eval)
                        beta = min(beta, curr_eval)
                        if beta < alpha:
                            break
                else:
                    for move in ghostlegalmoves:
                        ghost_gs = gs.generateSuccessor(ghost, move)
                        curr_eval = Alphabeta(ghost_gs, depth - 1, True, evaluationfunc, agents - 1, 1, False, alpha, beta)
                        min_eval = min(min_eval, curr_eval)
                        beta = min(beta, curr_eval)
                        if beta < alpha:
                            break
                    
                return min_eval
        
        final_eval = Alphabeta(gameState, self.depth, True, self.evaluationFunction, agents - 1, 1, True, -999999, 999999)
        

        for key, value in scores.items():
            if value == final_eval:
                return key
        
    
    

class ExpectimaxAgent(MultiAgentSearchAgent):
    """
      Your expectimax agent (question 4)
    """

    def getAction(self, gameState: GameState):
        """
        Returns the expectimax action using self.depth and self.evaluationFunction

        All ghosts should be modeled as choosing uniformly at random from their
        legal moves.
        """
        scores = {}
        agents = gameState.getNumAgents()
        
        def Expectimax(gs: GameState, depth, is_pac_bool, evaluationfunc, num_ghosts, ghost_i, record_value):
            
            if gs.isLose() or gs.isWin():
                return evaluationfunc(gs)
            
            if depth == 0 and is_pac_bool:
                return evaluationfunc(gs)
            
            if is_pac_bool:
                max_eval = -999999
                for child in gs.getLegalActions(0):
                    childGameState = gs.generateSuccessor(0, child)
                    curr_eval = Expectimax(childGameState, depth, False, evaluationfunc, num_ghosts, 1, False)
                    max_eval = max(max_eval, curr_eval)
                    if record_value:
                        scores[child] = curr_eval
                return max_eval
            
            else:
                expected_value = 0
                ghost = ghost_i
                ghostlegalmoves = gs.getLegalActions(ghost)
                
                if num_ghosts > ghost:
                    for move in ghostlegalmoves:
                        ghost_gs = gs.generateSuccessor(ghost, move)
                        curr_eval = Expectimax(ghost_gs, depth, False, evaluationfunc, num_ghosts, ghost + 1,  False)
                        expected_value += (1/len(ghostlegalmoves)) * curr_eval
                else:
                    for move in ghostlegalmoves:
                        ghost_gs = gs.generateSuccessor(ghost, move)
                        curr_eval = Expectimax(ghost_gs, depth - 1, True, evaluationfunc, agents - 1, 1, False)
                        expected_value += (1/len(ghostlegalmoves)) * curr_eval
                
                    
                return expected_value
        
        final_eval = Expectimax(gameState, self.depth, True, self.evaluationFunction, agents - 1, 1, True)
        

        for key, value in scores.items():
            if value == final_eval:
                return key

def betterEvaluationFunction(currentGameState: GameState):
    """
    Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
    evaluation function (question 5).

    DESCRIPTION: <write something here so we know what you did>
    """
    
    newPos = currentGameState.getPacmanPosition()
    newFood = currentGameState.getFood()
    newGhostStates = currentGameState.getGhostStates()
    newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]
    ghostpos = currentGameState.getGhostPosition(1)
    
    if len(currentGameState.getLegalActions()) == 1:
        return -999999999999
    
    capsule_dist = 0
    for cap in currentGameState.getCapsules():
        capsule_dist = manhattanDistance(newPos, cap)
        
    distbtwnghost = manhattanDistance(newPos, ghostpos)
    if distbtwnghost < 1:
        return -99999999999999
    
    dist_to_food = {}
    
    for food in newFood.asList() + currentGameState.getCapsules():
        dist = manhattanDistance(newPos, food)
        if dist < 1:
            return 99999999
        dist_to_food[food] = dist
    
    food_heuristic = 0
    if dist_to_food:
        min_food = min(dist_to_food, key = dist_to_food.get)
        food2food = {}
        
        for food in newFood.asList() + currentGameState.getCapsules():
            dist2 = manhattanDistance(min_food, food)
            food2food[food] = dist2
            
        max_food = max(food2food, key = food2food.get)
        food_heuristic = dist_to_food[min_food] + food2food[max_food]
    
    mean_food = 0
    for food in newFood.asList():
        mean_food += manhattanDistance(newPos, food)
    
    if food_heuristic and mean_food:
        return currentGameState.getScore()*0.5 + 100/food_heuristic + 100/mean_food
    else:
        return currentGameState.getScore()
        
    
    
    

# Abbreviation
better = betterEvaluationFunction

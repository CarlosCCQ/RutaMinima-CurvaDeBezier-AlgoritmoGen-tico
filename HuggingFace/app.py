from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
import math
import random

app = FastAPI()

class RequestData(BaseModel):
	data: List[float]
	data2: List[int]

class ResponseData(BaseModel):
	prediction: List[int]

class Individuo:
	def __init__(self, orden):
		self.orden = orden
		self.distancia = 0

	def calcular_distancia(self, puntos):
		self.distancia = sum(math.dist(puntos[self.orden[i]], puntos[self.orden[i+1]]) for i in range(len(self.orden) -1))
		return self.distancia

def generar_población(n, tam):
	poblacion = []
	for _ in range(tam):
		orden = list(range(n))
		random.shuffle(orden)
		poblacion.append(Individuo(orden))

	return poblacion

def cruzar(padre1, padre2):
	a, b = sorted(random.sample(range(len(padre1.orden)), 2))
	intermedio = padre1.orden[a:b]
	resto = [gen for gen in padre2.orden if gen not in intermedio]
	hijo = resto[:a] + intermedio + resto[a:]
	return Individuo(hijo)

def mutar(individuo, prob):
	for i in range(len(individuo.orden)):
		if random.random() < prob:
			j = random.randint(0, len(individuo.orden) -1)
			individuo.orden[i], individuo.orden[j] = individuo.orden[j], individuo.orden[i]

@app.post("/predict/")
def predict(data: RequestData):
	num_ciudades = int(data.data[0])
	tam_poblacion = int(data.data[1])
	prob_mutación = data.data[2]
	generaciones = int(data.data[3])

	coords = [(data.data2[i], data.data2[i+1]) for i in range(0, len(data.data2), 2)]
	poblacion = generar_población(num_ciudades, tam_poblacion)

	for ind in poblacion:
		ind.calcular_distancia(coords)

	for _ in range(generaciones):
		poblacion.sort(key=lambda x: x.distancia)
		nueva_gen = poblacion[:tam_poblacion//2]
		while len(nueva_gen) < tam_poblacion:
			padre1, padre2 = random.sample(nueva_gen, 2)
			hijo = cruzar(padre1, padre2)
			mutar(hijo, prob_mutación)
			hijo.calcular_distancia(coords)
			nueva_gen.append(hijo)

		poblacion = nueva_gen

	mejor = min(poblacion, key=lambda x: x.distancia)
	return ResponseData(prediction=mejor.orden)
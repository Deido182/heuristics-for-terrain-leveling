# Euristica_spianamento_terreni

Le classi "fondamentali" sono Truck, Field e i 3 Solver. Di questi, i primi 2 (Grasp e Our) applicano la stessa strategia, ma utilizzano 
diversi metodi di costruzione delle catene (vedere ChainsBuilder); il terzo invece utilizza un SW noto (LKH) per risolvere il problema. 
Il terzo di fatto non è altro che una sorta di classe interfaccia per l'utilizzo di (una delle tante funzioni offerte da) LKH.

ConvexHull, PathPrinter e InputBuilder sono classi che non contribuiscono all'ottenimento della soluzione. 
Sono state realizzate principalmente per la stesura della tesi (le prime due per fornire una sorta di "versione grafica" 
del risultato, mentre l'ultima per produrre nuovi test).

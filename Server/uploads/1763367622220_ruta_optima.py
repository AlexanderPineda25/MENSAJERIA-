import sys
import networkx as nx
import matplotlib.patheffects as pe
import matplotlib.pyplot as plt
from matplotlib.backends.backend_qt5agg import FigureCanvasQTAgg as FigureCanvas
from PyQt5.QtWidgets import (
    QApplication, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QLineEdit, QPushButton, QGridLayout, QMessageBox,
    QSpinBox, QTextEdit, QFrame, QFileDialog
)
from PyQt5.QtGui import QFont
from PyQt5.QtCore import Qt, QTimer


class GrafoApp(QWidget):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Generador de Grafos desde Matriz de Adyacencia")
        self.setGeometry(200, 100, 1100, 700)
        self.layout_principal = QHBoxLayout(self)

        left = QVBoxLayout()
        hbox_tamano = QHBoxLayout()
        hbox_tamano.addWidget(QLabel("Tamaño de la matriz (n × n):"))
        self.spin_n = QSpinBox()
        self.spin_n.setRange(2, 9999)
        self.spin_n.setValue(4)
        hbox_tamano.addWidget(self.spin_n)
        self.btn_generar_matriz = QPushButton("Crear matriz")
        self.btn_generar_matriz.clicked.connect(self.crear_campos_matriz)
        hbox_tamano.addWidget(self.btn_generar_matriz)
        left.addLayout(hbox_tamano)

        self.matriz_layout = QGridLayout()
        left.addLayout(self.matriz_layout)

        acciones = QHBoxLayout()
        self.btn_grafo = QPushButton("Generar Grafo")
        self.btn_grafo.setFont(QFont("Arial", 11, QFont.Bold))
        self.btn_grafo.setStyleSheet("background-color: #9ad0ec; padding: 6px; border-radius: 6px;")
        self.btn_grafo.clicked.connect(self.generar_grafo)
        acciones.addWidget(self.btn_grafo)
        self.btn_repr = QPushButton("Mostrar representación matemática")
        self.btn_repr.clicked.connect(self.mostrar_representacion)
        acciones.addWidget(self.btn_repr)
        left.addLayout(acciones)

        hbox_algoritmos = QHBoxLayout()
        self.btn_dijkstra = QPushButton("Ejecutar Dijkstra")
        self.btn_dijkstra.clicked.connect(self.ejecutar_dijkstra)
        hbox_algoritmos.addWidget(self.btn_dijkstra)

        self.btn_bellman = QPushButton("Ejecutar Bellman-Ford")
        self.btn_bellman.clicked.connect(self.ejecutar_bellman_ford)
        hbox_algoritmos.addWidget(self.btn_bellman)

        left.addLayout(hbox_algoritmos)

        self.btn_analizar = QPushButton("Analizar grafo")
        self.btn_analizar.clicked.connect(self.analizar_grafo)
        left.addWidget(self.btn_analizar)

        hbox_camino = QHBoxLayout()
        hbox_camino.addWidget(QLabel("Inicio:"))
        self.spin_inicio = QSpinBox()
        self.spin_n.setRange(2, 9999)
        self.spin_inicio.setValue(1)
        hbox_camino.addWidget(self.spin_inicio)
        hbox_camino.addWidget(QLabel("Fin:"))
        self.spin_fin = QSpinBox()
        self.spin_n.setRange(2, 9999)
        self.spin_fin.setValue(4)
        hbox_camino.addWidget(self.spin_fin)
        self.btn_buscar_camino = QPushButton("Resaltar camino más corto")
        self.btn_buscar_camino.clicked.connect(self.resaltar_camino_ui)
        hbox_camino.addWidget(self.btn_buscar_camino)
        left.addLayout(hbox_camino)

        hbox_ciclos = QHBoxLayout()
        self.btn_detectar_ciclos = QPushButton("Detectar y resaltar ciclos")
        self.btn_detectar_ciclos.clicked.connect(self.resaltar_ciclos_ui)
        hbox_ciclos.addWidget(self.btn_detectar_ciclos)

        self.btn_guardar_img = QPushButton("Guardar imagen del grafo")
        self.btn_guardar_img.clicked.connect(self.guardar_imagen)
        hbox_ciclos.addWidget(self.btn_guardar_img)

        left.addLayout(hbox_ciclos)

        self.lbl_info = QLabel(
            "💡 Detecta automáticamente si el grafo es dirigido o no. Puedes mover los nodos con el mouse.")
        self.lbl_info.setStyleSheet("color: gray; font-style: italic; margin-top: 5px;")
        left.addWidget(self.lbl_info)

        sep = QFrame()
        sep.setFrameShape(QFrame.HLine)
        sep.setFrameShadow(QFrame.Sunken)
        left.addWidget(sep)

        self.text_console = QTextEdit()
        self.text_console.setReadOnly(True)
        self.text_console.setFont(QFont("Courier", 10))
        left.addWidget(QLabel("Consola / Resultados:"))
        left.addWidget(self.text_console, stretch=1)
        self.layout_principal.addLayout(left, stretch=3)

        right = QVBoxLayout()
        self.fig, self.ax = plt.subplots(figsize=(6, 6))
        self.canvas = FigureCanvas(self.fig)
        right.addWidget(self.canvas)
        self.layout_principal.addLayout(right, stretch=5)

        self.campos = []
        self.crear_campos_matriz()
        self.G = None
        self.dirigido = False
        self.pos = None
        self.nodo_seleccionado = None
        self.highlight_edges = set()
        self.highlight_nodes = set()
        self.canvas.mpl_connect("button_press_event", self.on_click)
        self.canvas.mpl_connect("motion_notify_event", self.on_drag)
        self.canvas.mpl_connect("button_release_event", self.on_release)

        self.cargar_ejemplo_5x5()

    def crear_campos_matriz(self):
        for i in reversed(range(self.matriz_layout.count())):
            widget = self.matriz_layout.itemAt(i).widget()
            if widget:
                widget.deleteLater()
        self.campos = []
        n = self.spin_n.value()
        for i in range(n):
            fila = []
            for j in range(n):
                entrada = QLineEdit("0")
                entrada.setFixedWidth(45)
                entrada.setAlignment(Qt.AlignCenter)
                entrada.setFont(QFont("Courier", 11))
                self.matriz_layout.addWidget(entrada, i, j)
                fila.append(entrada)
            self.campos.append(fila)
        self.spin_inicio.setRange(1, n)
        self.spin_fin.setRange(1, n)

    def leer_matriz(self):
        try:
            n = len(self.campos)
            matriz = []
            for i in range(n):
                fila = []
                for j in range(n):
                    val = self.campos[i][j].text().strip()
                    if val == "":
                        val = "0"
                    if not (val.lstrip("-").isdigit()):
                        raise ValueError(f"El valor en la fila {i + 1}, columna {j + 1} no es un número válido.")
                    val = int(val)
                    fila.append(val)
                matriz.append(fila)
            return matriz
        except ValueError as e:
            QMessageBox.critical(self, "Error", f"Error de entrada: {e}")
            return None

    def es_dirigido(self, matriz):
        n = len(matriz)
        for i in range(n):
            for j in range(n):
                if matriz[i][j] != matriz[j][i]:
                    return True
        return False

    def validar_matriz(self, matriz):
        n = len(matriz)
        if not self.es_dirigido(matriz):
            for i in range(n):
                if matriz[i][i] != 0:
                    return False, "La diagonal debe ser cero para un grafo no dirigido."
        return True, ""

    def cargar_ejemplo_5x5(self):
        ejemplo = [
            [0, 1, 1, 0, 0],
            [0, 0, 1, 0, 1],
            [1, 0, 0, 1, 1],
            [0, 0, 0, 0, 0],
            [0, 0, 0, 1, 0],
        ]
        self.spin_n.setValue(5)
        self.crear_campos_matriz()
        for i in range(5):
            for j in range(5):
                self.campos[i][j].setText(str(ejemplo[i][j]))
        self.generar_grafo()

    def crear_grafo(self, matriz):
        dirigido = self.es_dirigido(matriz)
        G = nx.DiGraph() if dirigido else nx.Graph()
        n = len(matriz)
        G.add_nodes_from(range(1, n + 1))
        for i in range(n):
            for j in range(n):
                peso = matriz[i][j]
                if peso != 0:
                    G.add_edge(i + 1, j + 1, weight=peso)
        return G, dirigido

    def mostrar_grafo(self):
        self.ax.clear()
        if self.pos is None:
            self.pos = nx.spring_layout(self.G, seed=42, k=0.6)

        color_nodos = "#56c4c4"
        color_sombra = "#1a1a1a"
        color_aristas = "#444444"
        color_resaltado = "#e63946"
        fondo = "#f9f9f9"
        tam_nodo = 1500
        tam_flecha = 34
        ancho_arista = 2.6
        rad = 0.28
        min_margin = 12

        def _edge_kwargs_base(color=color_aristas, width=ancho_arista):
            kw = dict(edge_color=color, width=width, ax=self.ax)
            if self.dirigido:
                kw.update(
                    arrows=True,
                    arrowsize=tam_flecha,
                    arrowstyle='-|>',
                    min_source_margin=min_margin,
                    min_target_margin=min_margin,
                )
            else:
                kw.update(arrows=False)
            return kw

        def _edge_kwargs_curva(curva, color=color_aristas, width=ancho_arista):
            kw = _edge_kwargs_base(color=color, width=width)
            if self.dirigido and curva is not None:
                kw.update(connectionstyle=curva)
            return kw

        def _fmt_w(w):
            try:
                f = float(w)
                if abs(f - round(f)) < 1e-9:
                    return str(int(round(f)))
                return f"{f:.2f}".rstrip('0').rstrip('.')
            except Exception:
                return str(w)

        txt_box = dict(boxstyle="round,pad=0.25", facecolor="white",
                       edgecolor="#00000033", alpha=0.85)
        txt_effects = [pe.withStroke(linewidth=1.8, foreground="white")]

        sombra_pos = {n: (x + 0.015, y - 0.015) for n, (x, y) in self.pos.items()}
        nx.draw_networkx_nodes(self.G, sombra_pos, node_color=color_sombra,
                               node_size=tam_nodo, alpha=0.32, ax=self.ax)

        pesos = {(u, v): d.get("weight", None) for u, v, d in self.G.edges(data=True)}

        bidir_mismo = set()
        for (u, v), w in pesos.items():
            if (v, u) in pesos and pesos[(v, u)] == w:
                bidir_mismo.add(tuple(sorted((u, v))))

        for u, v in list(self.G.edges()):
            if u == v and (u, v) not in self.highlight_edges:
                curva = "arc3,rad=0.18" if self.dirigido else None
                nx.draw_networkx_edges(
                    self.G, self.pos, edgelist=[(u, v)],
                    **_edge_kwargs_curva(curva)
                )

        ya_dib = set()
        for u, v in bidir_mismo:
            if u == v:
                continue
            if (u, v) in ya_dib or (v, u) in ya_dib:
                continue
            nx.draw_networkx_edges(
                self.G, self.pos, edgelist=[(u, v)],
                ax=self.ax, edge_color=color_aristas, width=ancho_arista + 0.8,
                arrows=False
            )
            ya_dib.add((u, v))

        for u, v in self.G.edges():
            if u == v:
                continue
            if tuple(sorted((u, v))) in bidir_mismo:
                continue
            if (u, v) in self.highlight_edges:
                continue

            tiene_inversa = (v, u) in pesos
            curva = f"arc3,rad={rad}" if tiene_inversa else None
            nx.draw_networkx_edges(
                self.G, self.pos, edgelist=[(u, v)],
                **_edge_kwargs_curva(curva)
            )

        for (u, v) in list(self.highlight_edges):
            curva = f"arc3,rad={rad}" if (v, u) in self.highlight_edges else None
            kw = _edge_kwargs_curva(curva, color=color_resaltado, width=4.4)
            if self.dirigido:
                kw["arrowsize"] = tam_flecha + 6
            nx.draw_networkx_edges(self.G, self.pos, edgelist=[(u, v)], **kw)

        for u, v, d in self.G.edges(data=True):
            if u == v:
                x, y = self.pos[u]
                t = self.ax.text(x, y + 0.12, _fmt_w(d.get("weight", "")),
                                 fontsize=11, color='black', ha='center', va='center',
                                 bbox=txt_box)
                t.set_path_effects(txt_effects)

        for u, v in bidir_mismo:
            if u == v:
                continue
            w = pesos[(u, v)] if (u, v) in pesos else pesos[(v, u)]
            x = (self.pos[u][0] + self.pos[v][0]) / 2
            y = (self.pos[u][1] + self.pos[v][1]) / 2
            t = self.ax.text(x, y, _fmt_w(w), fontsize=11, color='black',
                             ha='center', va='center', bbox=txt_box)
            t.set_path_effects(txt_effects)

        for (u, v), w in pesos.items():
            if u == v:
                continue
            if tuple(sorted((u, v))) in bidir_mismo:
                continue

            x = (self.pos[u][0] + self.pos[v][0]) / 2
            y = (self.pos[u][1] + self.pos[v][1]) / 2
            offset = 0.06 if (v, u) in pesos else 0.0
            if (v, u) in pesos and u > v:
                offset = -offset

            t = self.ax.text(x, y + offset, _fmt_w(w), fontsize=11, color='black',
                             ha='center', va='center', bbox=txt_box)
            t.set_path_effects(txt_effects)

        nx.draw_networkx_nodes(self.G, self.pos, node_color=color_nodos,
                               node_size=tam_nodo, edgecolors="black",
                               linewidths=1.5, ax=self.ax)
        if self.highlight_nodes:
            nx.draw_networkx_nodes(
                self.G, self.pos,
                nodelist=list(self.highlight_nodes),
                node_color="#f77f00",
                node_size=tam_nodo * 1.1,
                edgecolors="black",
                linewidths=2,
                ax=self.ax
            )

        nx.draw_networkx_labels(self.G, self.pos, font_color="white",
                                font_weight="bold", font_size=13, ax=self.ax)

        tipo = "DIRIGIDO" if self.dirigido else "NO DIRIGIDO"
        self.ax.set_title(f"GRAFO {tipo}", fontsize=18, fontweight='bold',
                          color='#1d3557', pad=15)
        self.ax.axis("off")
        self.fig.set_facecolor(fondo)
        self.canvas.draw()

    def guardar_imagen(self):
        if self.G is None:
            QMessageBox.warning(self, "Sin grafo", "Genera primero el grafo.")
            return
        file_path, _ = QFileDialog.getSaveFileName(self, "Guardar imagen", "grafo.png", "Imágenes PNG (*.png)")
        if file_path:
            self.fig.savefig(file_path, dpi=300, bbox_inches="tight", facecolor="#f9f9f9")
            QMessageBox.information(self, "Imagen guardada", f"El grafo se guardó correctamente en:\n{file_path}")

    def generar_grafo(self):
        matriz = self.leer_matriz()
        if matriz is None:
            return
        valido, mensaje = self.validar_matriz(matriz)
        if not valido:
            QMessageBox.critical(self, "Error", mensaje)
            return
        self.highlight_edges = set()
        self.highlight_nodes = set()
        self.G, self.dirigido = self.crear_grafo(matriz)
        self.pos = nx.spring_layout(self.G, seed=42, k=0.6)
        self.mostrar_grafo()
        self.text_console.clear()
        self.text_console.append("Matriz de adyacencia:")
        self.text_console.append(self.formatear_matriz(matriz))
        self.text_console.append("")
        vertices = list(self.G.nodes())
        aristas = list(self.G.edges())
        self.text_console.append(f"V = {vertices}")
        self.text_console.append(f"E = {aristas}")
        self.text_console.append("")
        self.mostrar_lista_adyacencia()
        self.detectar_ciclos(silent=True)

    def formatear_matriz(self, matriz):
        return "\n".join(["\t".join(map(str, fila)) for fila in matriz])

    def mostrar_lista_adyacencia(self):
        lista = {}
        for nodo in self.G.nodes():
            adyacentes = list(self.G.neighbors(nodo))
            lista[nodo] = adyacentes

        self.text_console.append("Lista de adyacencia (representación visual):")

        texto = ""
        for nodo, ady in lista.items():
            flechas = " → ".join(map(str, ady)) if ady else "∅"
            texto += f"{nodo:>2} │  {flechas}\n"
        self.text_console.append(texto)

    def detectar_ciclos(self, silent=False):
        ciclos = []
        try:
            if self.dirigido:
                ciclos = list(nx.simple_cycles(self.G))
            else:
                ciclos = nx.cycle_basis(self.G)
            if ciclos:
                self.text_console.append(f"Ciclos detectados ({len(ciclos)}): {ciclos}")
                if not silent:
                    QMessageBox.information(self, "Ciclos Detectados", f"Se han detectado ciclos: {ciclos}")
            else:
                self.text_console.append("Sin ciclos detectados.")
                if not silent:
                    QMessageBox.information(self, "Sin Ciclos", "No se han detectado ciclos.")
        except Exception as e:
            self.text_console.append(f"Error detectando ciclos: {e}")
            if not silent:
                QMessageBox.critical(self, "Error", f"Error detectando ciclos: {e}")
        return ciclos

    def resaltar_ciclos_ui(self):
        if self.G is None:
            QMessageBox.warning(self, "Sin grafo", "Genera primero el grafo.")
            return
        ciclos = self.detectar_ciclos(silent=True)
        if not ciclos:
            QMessageBox.information(self, "Sin ciclos", "No se ha encontrado ningún ciclo para resaltar.")
            return
        self.highlight_edges.clear()
        self.highlight_nodes.clear()
        for ciclo in ciclos:
            if len(ciclo) < 2:
                continue
            edges_ciclo = []
            for i in range(len(ciclo)):
                u = ciclo[i]
                v = ciclo[(i + 1) % len(ciclo)]
                edges_ciclo.append((u, v))
            for e in edges_ciclo:
                self.highlight_edges.add(e)
                if not self.dirigido:
                    if (e[1], e[0]) in self.G.edges():
                        self.highlight_edges.add((e[1], e[0]))
                self.highlight_nodes.update(e)
        self.text_console.append(f"\nResaltando {len(ciclos)} ciclo(s).")
        self.mostrar_grafo()

    def tiene_pesos_negativos(self):
        if self.G is None:
            return False
        for _, _, datos in self.G.edges(data=True):
            if datos.get("weight", 1) < 0:
                return True
        return False

    def resaltar_camino_ui(self):
        if self.G is None:
            QMessageBox.warning(self, "Sin grafo", "Genera primero el grafo.")
            return
        inicio = int(self.spin_inicio.value())
        fin = int(self.spin_fin.value())
        if inicio not in self.G.nodes() or fin not in self.G.nodes():
            QMessageBox.warning(self, "Nodo inválido", "Selecciona nodos dentro del rango del grafo.")
            return
        if self.tiene_pesos_negativos():
            QMessageBox.warning(self, "Pesos negativos",
                                "El grafo contiene pesos negativos. Usa Bellman-Ford para encontrar el camino más corto.")
            self.text_console.append(
                "El grafo contiene aristas con peso negativo. Usa Bellman-Ford para el cálculo de la ruta más corta.")
            return
        try:
            if any("weight" in d for _, _, d in self.G.edges(data=True)):
                camino = nx.shortest_path(self.G, source=inicio, target=fin, weight="weight")
            else:
                camino = nx.shortest_path(self.G, source=inicio, target=fin)
            self.text_console.append(f"Camino encontrado entre {inicio} y {fin}: {camino}")
            self.highlight_edges.clear()
            self.highlight_nodes.clear()
            for i in range(len(camino) - 1):
                u = camino[i]
                v = camino[i + 1]
                self.highlight_edges.add((u, v))
                if not self.dirigido and (v, u) in self.G.edges():
                    self.highlight_edges.add((v, u))
                self.highlight_nodes.add(u)
                self.highlight_nodes.add(v)
            self.mostrar_grafo()
        except nx.NetworkXNoPath:
            self.text_console.append(f"No hay camino entre {inicio} y {fin}.")
            QMessageBox.warning(self, "No hay camino", f"No hay camino entre el nodo {inicio} y el nodo {fin}.")
        except Exception as e:
            self.text_console.append(f"Error buscando camino: {e}")
            QMessageBox.critical(self, "Error", f"Error buscando camino: {e}")

    def mostrar_representacion(self):
        if self.G is None:
            QMessageBox.warning(self, "Sin grafo", "Genera primero el grafo.")
            return
        vertices = list(self.G.nodes())
        aristas = list(self.G.edges(data=True))
        aristas_fmt = []
        for (u, v, d) in aristas:
            w = d.get("weight", "")
            if w != "":
                aristas_fmt.append(f"({u},{v}, w={w})")
            else:
                aristas_fmt.append(f"({u},{v})")
        texto = f"V = {vertices}\nE = {aristas_fmt}"
        self.text_console.append("\nRepresentación matemática:")
        self.text_console.append(texto)
        QMessageBox.information(self, "Representación Matemática", f"V = {vertices}\nE = {aristas_fmt}")

    def on_click(self, event):
        if event.inaxes != self.ax:
            return
        if event.xdata is None or event.ydata is None:
            return
        threshold = 0.04
        nearest = None
        nearest_dist = None
        for nodo, (x, y) in self.pos.items():
            dx = event.xdata - x
            dy = event.ydata - y
            dist2 = dx * dx + dy * dy
            if nearest is None or dist2 < nearest_dist:
                nearest = nodo
                nearest_dist = dist2
        if nearest is not None and nearest_dist < threshold:
            self.nodo_seleccionado = nearest
            self.mostrar_adyacentes(nearest)
        else:
            self.nodo_seleccionado = None

    def mostrar_adyacentes(self, nodo):
        if self.G is None:
            return
        adyacentes = list(self.G.neighbors(nodo))
        self.text_console.append(f"Nodos adyacentes al nodo {nodo}: {adyacentes}")

    def on_drag(self, event):
        if self.nodo_seleccionado is None or event.inaxes != self.ax:
            return
        if event.xdata is None or event.ydata is None:
            return
        self.pos[self.nodo_seleccionado] = [event.xdata, event.ydata]
        self.mostrar_grafo()

    def on_release(self, event):
        self.nodo_seleccionado = None

    def ejecutar_dijkstra(self):
        if self.G is None:
            QMessageBox.warning(self, "Sin grafo", "Genera primero el grafo.")
            return

        inicio = int(self.spin_inicio.value())
        fin = int(self.spin_fin.value())

        if inicio not in self.G.nodes() or fin not in self.G.nodes():
            QMessageBox.warning(self, "Nodo inválido", "Selecciona nodos dentro del rango del grafo.")
            return

        if self.tiene_pesos_negativos():
            QMessageBox.warning(self, "Pesos negativos",
                                "El grafo contiene pesos negativos. Dijkstra no es válido. Se ejecutará Bellman-Ford en su lugar.")
            self.text_console.append(
                "El grafo contiene aristas con peso negativo. Dijkstra no es aplicable. Ejecutando Bellman-Ford en su lugar.")
            self.ejecutar_bellman_ford()
            return

        dist = {n: float("inf") for n in self.G.nodes()}
        dist[inicio] = 0
        previo = {}
        visitados = set()

        self.text_console.append("\n=== Algoritmo de Dijkstra ===")
        self.text_console.append(f"Nodo de inicio: {inicio}, Nodo final: {fin}")
        self.text_console.append("Inicialización:")
        self.text_console.append(str(dist))

        while len(visitados) < len(self.G.nodes()):
            u = min((n for n in self.G.nodes() if n not in visitados), key=lambda x: dist[x])
            self.text_console.append(f"→ Se elige el nodo {u} con menor distancia no visitado ({dist[u]}).")
            visitados.add(u)

            vecinos = list(self.G.neighbors(u))
            self.text_console.append(f"→ Se actualizan los vecinos: {vecinos}")

            for v in vecinos:
                peso = self.G[u][v].get("weight", 1)
                if dist[u] + peso < dist[v]:
                    dist[v] = dist[u] + peso
                    previo[v] = u

            self.text_console.append(f"\nNodo visitado: {u}")
            tabla = "Nodo | Distancia\n" + "-" * 20 + "\n"
            for n in sorted(dist.keys()):
                d = "∞" if dist[n] == float("inf") else f"{dist[n]:.0f}"
                tabla += f"{n:^5} | {d:^9}\n"
            self.text_console.append(tabla)

        if fin not in previo and fin != inicio:
            QMessageBox.warning(self, "Sin camino", f"No hay camino entre {inicio} y {fin}.")
            self.text_console.append("No hay camino posible.")
            return

        camino = [fin]
        while camino[-1] != inicio:
            camino.append(previo[camino[-1]])
        camino.reverse()

        self.text_console.append(f"\nCamino óptimo encontrado: {camino}")
        self.text_console.append(f"Distancia mínima: {dist[fin]}")
        self.text_console.append(
            f"\n✅ El camino más corto desde {inicio} hasta {fin} es {camino}, "
            f"con un costo total de {dist[fin]}. "
            "Esto se obtuvo seleccionando sucesivamente el nodo con menor distancia acumulada."
        )

        self.highlight_edges.clear()
        for i in range(len(camino) - 1):
            self.highlight_edges.add((camino[i], camino[i + 1]))
            if not self.dirigido and (camino[i + 1], camino[i]) in self.G.edges():
                self.highlight_edges.add((camino[i + 1], camino[i]))

        self.highlight_nodes = set(camino)

        self.mostrar_grafo()

    def ejecutar_bellman_ford(self):
        if self.G is None:
            QMessageBox.warning(self, "Sin grafo", "Genera primero el grafo.")
            return

        inicio = int(self.spin_inicio.value())
        fin = int(self.spin_fin.value())

        if inicio not in self.G.nodes() or fin not in self.G.nodes():
            QMessageBox.warning(self, "Nodo inválido", "Selecciona nodos dentro del rango del grafo.")
            return

        dist = {n: float("inf") for n in self.G.nodes()}
        dist[inicio] = 0
        previo = {}

        self.text_console.append("\n=== Algoritmo de Bellman-Ford ===")
        self.text_console.append(f"Nodo de inicio: {inicio}, Nodo final: {fin}")
        self.text_console.append("Inicialización:")

        tabla = "Nodo | Distancia\n" + "-" * 20 + "\n"
        for n in sorted(dist.keys()):
            d = "∞" if dist[n] == float("inf") else f"{dist[n]:.0f}"
            tabla += f"{n:^5} | {d:^9}\n"
        self.text_console.append(tabla)

        for i in range(len(self.G.nodes()) - 1):
            self.text_console.append(f"\nIteración {i + 1}:")
            cambio = False
            for (u, v, datos) in self.G.edges(data=True):
                peso = datos.get("weight", 1)
                if dist[u] != float("inf") and dist[u] + peso < dist[v]:
                    dist[v] = dist[u] + peso
                    previo[v] = u
                    cambio = True
                    self.text_console.append(
                        f"→ Se relaja la arista ({u}, {v}) con peso {peso} → dist[{v}] = {dist[v]}"
                    )

            tabla = "Nodo | Distancia\n" + "-" * 20 + "\n"
            for n in sorted(dist.keys()):
                d = "∞" if dist[n] == float("inf") else f"{dist[n]:.0f}"
                tabla += f"{n:^5} | {d:^9}\n"
            self.text_console.append(tabla)

            if not cambio:
                self.text_console.append("No hubo cambios en esta iteración → el algoritmo converge antes de tiempo.")
                break

        for (u, v, datos) in self.G.edges(data=True):
            peso = datos.get("weight", 1)
            if dist[u] != float("inf") and dist[u] + peso < dist[v]:
                QMessageBox.critical(self, "Error", "El grafo contiene un ciclo negativo.")
                self.text_console.append("❌ Se detectó un ciclo negativo. No hay solución válida.")
                return

        if fin not in previo and fin != inicio:
            QMessageBox.warning(self, "Sin camino", f"No hay camino entre {inicio} y {fin}.")
            self.text_console.append("No hay camino posible.")
            return

        camino = [fin]
        while camino[-1] != inicio:
            camino.append(previo[camino[-1]])
        camino.reverse()

        self.text_console.append(f"\nCamino óptimo encontrado: {camino}")
        self.text_console.append(f"Distancia mínima: {dist[fin]}")
        self.text_console.append(
            f"\n✅ El camino más corto desde {inicio} hasta {fin} es {camino}, "
            f"con un costo total de {dist[fin]}. "
            "Esto se obtuvo relajando sucesivamente las aristas y actualizando las distancias acumuladas."
        )

        self.highlight_edges.clear()
        self.highlight_nodes.clear()
        for i in range(len(camino) - 1):
            self.highlight_edges.add((camino[i], camino[i + 1]))
            if not self.dirigido and (camino[i + 1], camino[i]) in self.G.edges():
                self.highlight_edges.add((camino[i + 1], camino[i]))
        self.highlight_nodes = set(camino)

        self.mostrar_grafo()

    def analizar_grafo(self):
        if self.G is None:
            QMessageBox.warning(self, "Sin grafo", "Genera primero el grafo.")
            return

        info = []
        info.append(f"Tipo: {'Dirigido' if self.dirigido else 'No dirigido'}")
        info.append(f"Número de nodos: {self.G.number_of_nodes()}")
        info.append(f"Número de aristas: {self.G.number_of_edges()}")

        grados = {n: self.G.degree(n) for n in self.G.nodes()}
        info.append(f"Grados de los nodos: {grados}")

        conexo = nx.is_weakly_connected(self.G) if self.dirigido else nx.is_connected(self.G)
        info.append(f"Conexo: {'Sí' if conexo else 'No'}")

        self.text_console.append("\n=== Análisis del Grafo ===")
        for linea in info:
            self.text_console.append(linea)

        QMessageBox.information(self, "Análisis del Grafo", "\n".join(info))

    def animar_camino(self, pasos, final_callback=None, intervalo=800):
        self.anim_pasos = pasos
        self.anim_index = 0
        self.anim_final_callback = final_callback
        self.anim_timer = QTimer()
        self.anim_timer.timeout.connect(self._animar_paso)
        self.anim_timer.start(intervalo)

    def _animar_paso(self):
        if self.anim_index >= len(self.anim_pasos):
            self.anim_timer.stop()
            if self.anim_final_callback:
                self.anim_final_callback()
            return

        visitados, dist, nodo_visitado, previo = self.anim_pasos[self.anim_index]

        self.highlight_nodes = set(visitados)

        self.highlight_edges.clear()
        for v, u in previo.items():
            self.highlight_edges.add((u, v))
            if not self.dirigido and (v, u) in self.G.edges():
                self.highlight_edges.add((v, u))

        self.text_console.append(f"\nPaso {self.anim_index + 1}: Nodo visitado {nodo_visitado}")
        tabla = "Nodo | Distancia\n" + "-" * 20 + "\n"
        for n in sorted(dist.keys()):
            d = "∞" if dist[n] == float("inf") else f"{dist[n]:.0f}"
            tabla += f"{n:^5} | {d:^9}\n"
        self.text_console.append(tabla)

        self.mostrar_grafo()

        self.anim_index += 1


if __name__ == "__main__":
    app = QApplication(sys.argv)
    ventana = GrafoApp()
    ventana.show()
    sys.exit(app.exec_())

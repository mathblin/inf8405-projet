# Serveur pour le projet robot

## Quelle est son utilité?
L'objectif du serveur est de recevoir les commandes envoyées depuis l'application mobile via des sockets et les transmettre au Arduino via une communication sérielle. L'[Arduino](../Arduino/) exécutera ensuite ces commandes. Il peut s'agir de commandes qui le font avancer, reculer, faire une rotation de $360^\circ$, etc.

## Librairies utilisées
Un fichier de requis pour le serveur est fourni avec le dépôt. Toutes les librairies utilisées y sont présentes. Pour le moment, seul `pyserial` n'est pas une librairie intégrée dans Python. Nous l'utilisons pour le serveur. Vous pouvez l'installer avec le fichier de requis nommé `requirements.txt`. Il est dans le dossier `Server`. Vous pouvez utiliser la commande `pip install -r requirements.txt`. La librairie `socket` est également utilisée. Néanmoins, elle semble être intégrée dans Python, du moins pour certaines versions de Python. Selon la documentation de la librairie [socket](https://docs.python.org/3/library/socket.html), vous devez avoir au moins la version 2.7 pour Python 2 ou 3.5 pour Python 3. Attention, il s'agit de `socket` sans `s`, donc pas `sockets`. `socket` est une interface réseau de bas niveau.

## Le fichier d'entrée du serveur 
Le fichier d'entrée que vous devez exécuter est `server.py`. Si vous désirez connaître les drapeaux/arguments (flags) possibles à passer au serveur, vous pouvez exécuter `python server.py -h` sur Windows ou `python3 server.py -h` sur linux. Vous pouvez également utiliser `--help`, ce qui revient au même.

## Drapeaux/arguments
Les objectifs principaux des drapeaux sont changer l'adresse IP du serveur pour qu'elle corresponde à celle de l'ordinateur (portable, tour, Raspberry PI), déboguer avec des affichages de console, tester la communication sérielle, etc. Si vous avez toujours des doutes sur comment fonctionnent les drapeaux/arguments, suivez la section `Le fichier d'entrée du serveur`.

## Plusieurs modes
Il est possible de tester la communication sérielle en ajoutant le drapeau `-serial` lorsque vous appellez le serveur. De cette manière, vous pouvez tester de nouvelles commandes Arduino. Conséquemment, vous devrez ajuster le code d'Arduino. Sans le drapeau `-serial`, le mode est avec socket. Dans ce mode, vous aurez besoin de l'application mobile installée sur un téléphone ou sur une tablette afin que le serveur reçoive des commandes.

## L'adresse IP
L'adresse IP doit être configurée dans l'application mobile. Elle doit corresponde avec celle du serveur. Pour la partie serveur, you devrez soit changer l'adresse IP par défaut dans le code, soit utiliser le drapeau `-ip` suivi de l'adresse. Par exemple, en faisant ceci: `python server.py -ip 127.0.0.1`. Vous pouvez chercher la variable suivante dans le code:
```python 
DEFAULT_HOST
```
Si elle n'a pas été déplacée depuis, elle devrait se trouver dans le fichier `server_flags.py`. L'adresse par défaut que nous avons utilisée est celle que `Smol` avait à l'université Polytechnique Montréal. Il est fortement possible qu'elle ait changée lorsque vous lirez ceci. Vous aurez à communiquer avec le département informatique si vous y êtes étudiant(e)s.

## Caméra
Le script [camera.py](camera.py) est repris du code déjà fourni par le git de Bilal. Il permet d'activer la caméra du Raspberry Pi. Ensuite, vous pourrez vous connecter par l'application mobile. Nous avons utilisé le port `8000`. Bien sûr, il faut l'exécuter pour que l'application mobile puisse se connecter à la caméra.

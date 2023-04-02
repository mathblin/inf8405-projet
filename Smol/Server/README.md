# Server for Robot project

## Quelle est sont utilité?
L'objectif du serveur est de recevoir les commandes envoyées depuis l'application mobile via sockets et les transmettre au Arduino via communication sérielle. L'Arduino exécutera ensuite ces commands. Il peut s'agir de commandes qui le font avancer, reculer, faire une rotation de $360^\circ$, etc.

## Le fichier d'entrée du serveur
Le fichier d'entrée qu'on doit exécuter est `server.py`. Si vous désirez connaître les drapeaux/arguments (flags) possibles à passer au serveur, vous pouvez exécuter `python server.py -h` sur Windows ou `python3 server.py -h` sur linux. Vous pouvez également utiliser `--help`, ce qui revient au même.

## Drapeaux/arguments
Les objectifs principaux des drapeaux sont changer l'adresse IP du serveur pour qu'elle corresponde à celle de l'ordinateur (portable, tour, Raspberry PI), déboguer avec des affichages de console, tester la communication sérielle, etc. Si vous avez toujours des doutes sur comment fonctionnent les drapeaux/arguments, suivez la section `Main entryfile`.

## Plusieurs modes
Il est possible de tester la communication sérielle en ajoutant le drapeau `-serial` lorsqu'on appelle le serveur. De cette manière, vous pouvez tester de nouvelles commandes Arduino. Conséquemment, vous devrez ajuster le code d'Arduino. Sans le drapeau `-serial`, le mode est avec socket. Dans ce mode, vous aurez besoin de l'application mobile installée sur un téléphone ou une tablette afin que le serveur reçoive des commandes.

## L'addresse IP
L'addresse IP doit être configurée dans l'application mobile. Elle doit corresponde avec celle du serveur. Pour la partie serveur, you devrez soit changer l'adresse IP par défaut dans le code, soit utiliser le drapeau `-ip` suivi de l'adresse. Par exemple, en faisant ceci: `python server.py -ip 127.0.0.1`. Vous pouvez chercher la variable suivante dans le code:
```python 
DEFAULT_HOST
```
Si elle n'a pas été déplacée depuis, elle devrait se trouver dans le fichier `server_flags.py`. L'adresse par défaut que nous avons utilisée est celle que `Smol` avait à l'université Polytechnique Montréal. Il est fortement possible qu'elle est changée lorsque vous lirez ceci. Vous aurez à communiquer avec le département informatique si vous y êtes étudiant(e)s.

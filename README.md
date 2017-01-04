# Github Client
A github client built with [Hoplon][hoplon]

## Objective
This project an example of using a Datascript db as the central storage for a
Hoplon app. The central storage allows snapshoting of the whole app state and
possible restoring it for debuging purposes.

Changes in state should only happen through events making it possible to replay
events so you could not only restore the whole state but also replay events and
seeing the updated app.

Some inspiration for this project comes from [re-frame][re-frame]. The re-frame
authors mention Hoplon as an inspiration for re-frame itself so this tries to
makes it a full circle with re-frame ideas flowing back to Hoplon projects.

This project is an github client written in Hoplon to show how those pieces go
together.

## Benefactor
The `benefactor.*` namespaces will be extracted to [benefactor][benefactor]
after they are stable enough.

You know that code that you have to keep writing to deal with cookies, local
storage, json serializing, keycodes, routing and other small things? I don't
want to keep rewriting or fing it on old projects so the intention is to put
all that in library.

## Javelin + Datascript
If you put a datascript db on a javelin cell you get some magic flowing values
from a db. It doesn't sound like much but it is pretty cool! There a couple of
other helper functions to deal with nil values because you may end passing nils
because other cells updated.  Those goods are on the javelin.datascript
namespace.

## Development
You need boot installed and then run:
`boot dev`

[hoplon]: https://hoplon.io
[re-frame]: https://github.com/Day8/re-frame
[benefactor]: https://github.com/mynomoto/benefactor

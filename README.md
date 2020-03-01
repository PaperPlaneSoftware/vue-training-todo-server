# REST API

__Base URL__: https://vue-training-todo.herokuapp.com/

__Todo JSON__:
```json
{
    id: <int>,
    who: <string>,
    task: <string>,
    complete: <boolean>
}
```

__Endpoints__

| Method | Endpoint                         | Notes
|--------|----------------------------------|-------------------------|
| GET    | todo/all                         |                         |
| GET    | todo/<int>                       | <int> is Todo ID        | 
| GET    | todo/page/<int>?search=<string>  | Page size is 10         |
| POST   | todo                             | Expecting Todo JSON     |
| PUT    | todo                             | Expecting Todo JSON     |
| DELETE | todo/<int>                       | <int> is Todo ID        |
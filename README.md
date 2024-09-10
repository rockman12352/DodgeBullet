# Dodge Bullet!

### Instructions

You are in the Wild West and bullets will coming anywhere, please provide your instructions to dodge them all.

#### Endpoint
Expose a `POST` endpoint `/dodge` for us to call

#### Input

You will be given the following `text` as map with every attempt:

1. `.` means empty area
1. `u` `d` `r` `l` represent bullets with its direction up, down, right or left
1. `*` is where you located

```
.dd
r*.
...
```

#### Output Expected

Expected `JSON` to be returned

```json5
{
  "instructions": ["d", "l"]
}
```

### Rules

1. Bullets will move to next cell each time as you moved

    ```
    ...
    .dd
    .*.
    ```
    This is the output when you move down with sample Input
1. Bullets can overlap with each other
1. You can't dodge towards bullet

    given input:
    ```
    .d
    d*
    ```

   you can move `left` to dodge the bullet

   you **can't** move `up` to dodge the bullet
1. If no way to dodge all bullets, please provide null as instructions
    ```json5
    {
      "instructions": null
    }
    ```
1. You can't move out of the map
1. Challenge will be sending one by one, you need to pass them all
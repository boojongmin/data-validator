## Data validator


#### Data
```json
{
	"str": "hello world!",
	"num": 1,
	"arr": ["1", "2", "3"],
	"obj": {
		"arr": [
			{"key": "value"}, {"key": "value"}
		]
	}
}
```
#### Validate rule
```properties
@object.str.@string
@object.num.@number
@object.arr.@array.@string
@object.obj.@objet.arr.@array.@object.key.@string
```

```yaml
/* yaml 생각중.. */
@object:
	str: @string
	num: @number
	arr:
		@array:
			@string
	obj:
		@object:
			arr:
				@array:
					@object:
						key:
							@string
```

### Support Type
@object
@array
- value
	- @string
	- @number
	- @boolean
	- null(hm.....)

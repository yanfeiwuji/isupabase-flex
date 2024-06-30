# Limitations

## RPC functions do not handle arguments of type `List<T>` or `Map<K,V>`.

Using signatures with list or map types will cause Supabase's TypeScript to malfunction.

## Currently, cannot handle generics; all are processed as JSON types. However, it can handle generic `List<T>` .

To minimize issues, the next version will optimize handling for this problem. We are exploring a method to align
Supabase TypeScript and Java generics for better compatibility.

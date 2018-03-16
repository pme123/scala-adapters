{% for p in site.pages %}
  {% if p.categories contains page.category %}
* [{{ p.title }}]({{ p.url | absolute_url }})  
  <small>{{ p.excerpt }}</small>
  {% endif %}
{% endfor %}
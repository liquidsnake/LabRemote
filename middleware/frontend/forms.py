from django.forms import ModelForm
from middleware.core import models

class GroupForm(ModelForm):
    class Meta:
        model = models.Group
        exclude = ('students',)
